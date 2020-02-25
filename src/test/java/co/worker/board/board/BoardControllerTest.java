package co.worker.board.board;

import co.worker.board.board.model.BoardEntity;
import co.worker.board.board.model.BoardParam;
import co.worker.board.board.repository.BoardRepository;
import co.worker.board.user.model.UserEntity;
import co.worker.board.user.model.UserParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BoardControllerTest {
    MockMvc mockMvc;
    @Autowired
    BoardRepository boardRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(); // (1)

    @Autowired
    private WebApplicationContext context;

    private RestDocumentationResultHandler document;



    @Before
    public void setUpAndInsertBoard(){
        this.document = document(
                "{class-name}/{method-name}",
                preprocessResponse(prettyPrint()));

        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).apply(documentationConfiguration(this.restDocumentation))
                .alwaysDo(document).build();

        for(int i =1; i<=20; i++){
            UserEntity user = UserEntity.builder().id("doqndnf"+i).name("유저"+i).password("tjdghks"+i+"!").savedTime(LocalDateTime.now(ZoneId.of("Asia/Seoul"))).build();
            BoardEntity boardEntity = BoardEntity.builder().content("내용"+i).title("제목"+i).userEntity(user).savedTime(LocalDateTime.now(ZoneId.of("Asia/Seoul"))).build();
            boardRepository.save(boardEntity);
        }
    }

    @Test
    public void addBoard() throws Exception {
        UserParam user = UserParam.builder().id("addId").name("유저추가").password("비밀번호").savedTime(LocalDateTime.now(ZoneId.of("Asia/Seoul"))).build();

        BoardParam param = BoardParam.builder()
                .content("추가내용")
                .title("추가제목")
                .user(user)
                .savedTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        mockMvc.perform(post("/api/boards/add")
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8")
                .content(objectMapper.writeValueAsString(param)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                    requestFields(
                            fieldWithPath("seq").type(JsonFieldType.NULL).description("글 번호"),
                            fieldWithPath("content").type(JsonFieldType.STRING).description("글 내용"),
                            fieldWithPath("title").type(JsonFieldType.STRING).description("글 제목"),
                            fieldWithPath("savedTime").type(JsonFieldType.STRING).description("글 작성일").attributes(new Attributes.Attribute("format","yyyy-MM-dd HH:mm:ss")),
                            fieldWithPath("user.seq").type(JsonFieldType.NULL).description("유저 시퀀스"),
                            fieldWithPath("user.id").type(JsonFieldType.STRING).description("유저 아이디"),
                            fieldWithPath("user.name").type(JsonFieldType.STRING).description("유저 이름"),
                            fieldWithPath("user.savedTime").type(JsonFieldType.STRING).description("유저 가입일").attributes(new Attributes.Attribute("format","yyyy-MM-dd HH:mm:ss"))
                    ),
                    responseFields(
                            fieldWithPath("code").type(JsonFieldType.NUMBER).description("결과코드(200 : 성공)"),
                            fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                            fieldWithPath("result").type(JsonFieldType.NULL).description("결과 객체")
                    )))
                .andExpect(jsonPath("$.code", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(notNullValue())))
        ;
        this.getBoard();
    }

    @Test
    public void editBoard() throws Exception{
        UserParam user = UserParam.builder().id("editId").name("유저수정").password("비밀번호").savedTime(LocalDateTime.now(ZoneId.of("Asia/Seoul"))).build();

        BoardParam param = BoardParam.builder()
                .content("수정내용")
                .title("수정제목")
                .user(user).savedTime(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        mockMvc.perform(put("/api/boards/{seq}",3)
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8")
                .content(objectMapper.writeValueAsString(param)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("seq").description("게시판 순번")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("Http 상태값"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("성공유무 메시지"),
                                fieldWithPath("result").type(JsonFieldType.NULL).description("결과값")
                        )
                ))
                .andExpect(jsonPath("$.code", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(notNullValue())))
        ;

    }

    @Test
    public void getBoard() throws Exception {
        mockMvc.perform(get("/api/boards/list/{page}",1)
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("page").description("페이지 넘버")
                        ),
                        responseFields(
                                fieldWithPath("code").description("Http 상태값"),
                                fieldWithPath("message").description("성공유무 메시지"),
                                fieldWithPath("result[].seq").description("게시판 순번"),
                                fieldWithPath("result[].content").description("게시판 글내용"),
                                fieldWithPath("result[].title").description("게시판 제목"),
                                fieldWithPath("result[].user.seq").description("게시판 작성자 순번"),
                                fieldWithPath("result[].user.id").description("게시판 작성자 아이디"),
                                fieldWithPath("result[].user.name").description("게시판 작성자명"),
                                fieldWithPath("result[].user.savedTime").description("게시판 작성자 가입일"),
                                fieldWithPath("result[].savedTime").description("게시판 등록일")
                        )
                ))
                .andExpect(jsonPath("$.code", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].seq", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].content", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].title", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].user.seq", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].user.id", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].user.name", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].user.savedTime", is(notNullValue())))
                .andExpect(jsonPath("$.result[*].savedTime", is(notNullValue())))
        ;

    }

    @Test
    public void getBoardOne1() throws Exception {
        mockMvc.perform(get("/api/boards/{seq}", 1)
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getBoardOne2() throws Exception {
        mockMvc.perform(get("/api/boards/{seq}", 1)
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("seq").description("Board`s seq")
                        ),
                        responseFields(
                                fieldWithPath("code").description("The HttpStatus code"),
                                fieldWithPath("message").description("The Http`s message"),
                                fieldWithPath("result.seq").description("The Board`s seq"),
                                fieldWithPath("result.content").description("The Board`s content"),
                                fieldWithPath("result.title").description("The Board`s title"),
                                fieldWithPath("result.user.seq").description("The Board`s user seq"),
                                fieldWithPath("result.user.id").description("The Board`s user id"),
                                fieldWithPath("result.user.name").description("The Board`s user name"),
                                fieldWithPath("result.user.savedTime").description("The Board`s user regdate"),
                                fieldWithPath("result.savedTime").description("The Board`s regdate")
                        )
                ))
                .andExpect(jsonPath("$.code", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(notNullValue())))
                .andExpect(jsonPath("$.result.content", is(notNullValue())))
                .andExpect(jsonPath("$.result.user.seq", is(notNullValue())))
                .andExpect(jsonPath("$.result.user.id", is(notNullValue())))
                .andExpect(jsonPath("$.result.user.name", is(notNullValue())))
                .andExpect(jsonPath("$.result.user.savedTime", is(notNullValue())))
                .andExpect(jsonPath("$.result.title", is(notNullValue())))
                .andExpect(jsonPath("$.result.savedTime", is(notNullValue())))
        ;
    }

    @Test
    public void deleteBoardOne() throws Exception{
        mockMvc.perform(delete("/api/boards/{seq}", 3)
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("seq").description("글 시퀀스")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("결과코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지"),
                                fieldWithPath("result").type(JsonFieldType.NULL).description("결과값")
                        )
                ))
                .andExpect(jsonPath("$.code", is(notNullValue())))
                .andExpect(jsonPath("$.message", is(notNullValue())))
        ;

    }

    //Bad_Request 테스트
    @Test //add
    public void board_BadRequest_add() throws Exception{
        //BoardParam param = BoardParam.builder().content("test").title("test").build(); //username not null

        UserParam user = UserParam.builder().id("addId").name("유저추가").password("비밀번호").build();
        BoardParam param = BoardParam.builder().title("title").content("").user(user).build(); // content not empty

        mockMvc.perform(post("/api/boards/add")
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8")
                .content(objectMapper.writeValueAsString(param)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void board_BadRequest_getOne() throws Exception{ // 문제 수정해야됨.
        // 400이 나와야하는데 200이 나옴. -> ok메소드를 실행해서 그럼 -> 오류 -> 400이 나오도록 수정.
        mockMvc.perform(get("/api/boards/-1")
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void board_BadRequest_edit() throws Exception {
        BoardParam param = BoardParam.builder().content("test").title("test").build(); //  username null
        //BoardParam param = BoardParam.builder().content("test").title("test").user(user).build(); // seq min 0
        //UserParam user = UserParam.builder().id("addId").name("유저추가").password("비밀번호").build();
        mockMvc.perform(put("/api/boards/0")
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8")
                .content(objectMapper.writeValueAsString(param)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void board_delete_not_exist_seq() throws Exception{
        mockMvc.perform(delete("/api/boards/{seq}",3000)
                .contentType("application/json;charset=utf-8")
                .accept("application/json;charset=utf-8"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andDo(document.document(
                        pathParameters(
                                parameterWithName("seq").description("글 시퀀스")
                        ),
                        responseFields(
                                fieldWithPath("code").type(JsonFieldType.NUMBER).description("결과코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메시지")
                        )
                ))
                .andExpect(jsonPath("$.code",is(notNullValue())))
                .andExpect(jsonPath("$.message",is(notNullValue())))
        ;
    }

}