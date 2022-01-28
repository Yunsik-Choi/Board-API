package com.project.barter.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.barter.board.dto.BoardPost;
import com.project.barter.global.GlobalConst;
import com.project.barter.user.User;
import com.project.barter.user.UserRepository;
import com.project.barter.user.UserUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.ServletContext;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
class BoardIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ServletContext servletContext;

    @Order(1)
    @DisplayName("게시물 쓰기")
    @Test
    public void Board_Write() throws Exception {
        BoardPost boardPost = BoardPost.builder().title("제목").content("내용").build();
        User save = userRepository.save(UserUtils.getCompleteUser());

        mockMvc.perform(post("/board")
                .session(setSession(save))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardPost)))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"))
                .andDo(document("Board Write",
                        requestFields(
                                fieldWithPath("title").description("게시물 제목"),
                                fieldWithPath("content").description("게시물 내용")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("redirect url")
                        )
                ));
    }

    @DisplayName("로그인 하지 않은 사용자가 게시물 작성을 시도할 경우 401에러를 반환한다.")
    @Test
    public void Board_Write_UnAuthorized() throws Exception {
        BoardPost boardPost = BoardPost.builder().title("제목").content("내용").build();

        mockMvc.perform(post("/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardPost)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("Board Write UnAuthorized",
                        requestFields(
                                fieldWithPath("title").description("게시물 제목"),
                                fieldWithPath("content").description("게시물 내용")
                        )
                ));
    }

    @DisplayName("로그인 하지 않은 사용자도 게시물 조회가 가능하다.")
    @Test
    public void Board_GET_UnAuthorized() throws Exception {
        mockMvc.perform(get("/board/{id}",1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board 로그인하지 않은 사용자 조회",
                        pathParameters(
                                parameterWithName("id").description("게시물 식별자")
                        ),
                        responseFields(
                                fieldWithPath("id").description("게시물 식별자"),
                                fieldWithPath("title").description("게시물 제목"),
                                fieldWithPath("content").description("게시물 내용"),
                                fieldWithPath("writeTime").description("게시물 작성 시간"),
                                subsectionWithPath("user").description("게시물 작성 유저 정보"),
                                subsectionWithPath("commentList.[]").description("게시물 댓글 리스트")

                        )
                ));
    }

    @DisplayName("게시물 식별자로 조회")
    @Test
    public void Board_Find_By_Id() throws Exception {
        mockMvc.perform(get("/board/{id}",1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board 식별자로 조회",
                    pathParameters(
                            parameterWithName("id").description("게시물 식별자")
                    ),
                    responseFields(
                            fieldWithPath("id").description("게시물 식별자"),
                            fieldWithPath("title").description("게시물 제목"),
                            fieldWithPath("content").description("게시물 내용"),
                            fieldWithPath("writeTime").description("게시물 작성 시간"),
                            subsectionWithPath("user").description("게시물 작성 유저 정보"),
                            subsectionWithPath("commentList.[]").description("게시물 댓글 리스트")
                    )
                ));
    }

    @DisplayName("게시물 존재하지 않는 식별자로 조회시 404에러 반환")
    @Test
    public void Board_Find_By_Id_No_Exists() throws Exception {
        mockMvc.perform(get("/board/{id}",Long.MAX_VALUE))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document("Board 존재하지 않는 식별자로 조회",
                        pathParameters(
                                parameterWithName("id").description("게시물 식별자")
                        )
                ));
    }

    @DisplayName("게시물 전체 조회")
    @Test
    public void Board_Find_All() throws Exception {
        mockMvc.perform(get("/board"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board 전체 조회",
                        responseFields(
                                fieldWithPath("[]").description("게시물 리스트"),
                                fieldWithPath("[].id").description("게시물 식별자"),
                                fieldWithPath("[].title").description("게시물 제목"),
                                fieldWithPath("[].content").description("게시물 내용"),
                                fieldWithPath("[].writeTime").description("게시물 작성 시간"),
                                subsectionWithPath("[].user").description("게시물 작성 유저 정보")
                        )
                ));
    }

    @DisplayName("게시물 미리보기 전체 조회")
    @Test
    public void Board_Find_All_Preview() throws Exception {
        mockMvc.perform(get("/board/preview"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board 미리보기 전체 조회",
                        responseFields(
                                fieldWithPath("[]").description("게시물 리스트"),
                                fieldWithPath("[].id").description("게시물 식별자"),
                                fieldWithPath("[].title").description("게시물 제목"),
                                fieldWithPath("[].writeTime").description("게시물 작성 시간"),
                                fieldWithPath("[].loginId").description("게시물 작성자 로그인 아이디")
                        )
                ));
    }

    @DisplayName("게시물에 댓글 남기기")
    @Test
    public void Board_Add_Comment() throws Exception {
        User saveUser = userRepository.findById(1L).get();
        Board saveBoard = boardRepository.save(BoardUtils.getCompleteBoard());
        mockMvc.perform(post("/board/{id}/comment",saveBoard.getId())
                .session(setSession(saveUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(BoardUtils.getCommentPost())))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"))
                .andDo(document("게시물에 댓글 남기기",
                        requestFields(
                                fieldWithPath("content").description("댓글 내용")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("redirect url")
                        )
                ));
    }

    private MockHttpSession setSession(User user) {
        MockHttpSession mockHttpSession = new MockHttpSession(servletContext);
        mockHttpSession.setAttribute(GlobalConst.loginSessionAttributeName,user.getLoginId());
        return mockHttpSession;
    }
}