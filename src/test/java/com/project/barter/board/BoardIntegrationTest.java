package com.project.barter.board;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.barter.board.dto.BoardPost;
import com.project.barter.comment.CommentRepository;
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
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
class BoardIntegrationTest extends BoardField{

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    BoardRepository boardRepository;
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ServletContext servletContext;

    @Order(1)
    @DisplayName("????????? ??????")
    @Test
    public void Board_Write() throws Exception {
        BoardPost boardPost = BoardPost.builder().title("??????").content("??????").build();
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
                                fieldWithPath("title").description("????????? ??????"),
                                fieldWithPath("content").description("????????? ??????")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("redirect url")
                        )
                ));
    }

    @Order(2)
    @DisplayName("???????????? ?????? ?????????")
    @Test
    public void Board_Add_Comment() throws Exception {
        User saveUser = userRepository.findById(1L).get();
        mockMvc.perform(post("/board/{id}/comment",1L)
                .session(setSession(saveUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(BoardUtils.getCommentPost())))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"))
                .andDo(document("???????????? ?????? ?????????",
                        pathParameters(
                                parameterWithName("id").description("????????? ?????????")
                        ),
                        requestFields(
                                fieldWithPath("content").description("?????? ??????")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("redirect url")
                        )
                ));
    }

    @Order(3)
    @DisplayName("???????????? ????????? ?????????")
    @Test
    public void Board_Add_SubComment() throws Exception {
        User saveUser = userRepository.findById(1L).get();

        mockMvc.perform(post("/board/{boardId}/comment/{commentId}/subcomment",1L,1L)
                .session(setSession(saveUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(BoardUtils.getCommentPost())))
                .andDo(print())
                .andExpect(status().isFound())
                .andExpect(header().exists("Location"))
                .andDo(document("???????????? ?????? ?????????",
                        pathParameters(
                                parameterWithName("boardId").description("????????? ?????????"),
                                parameterWithName("commentId").description("?????? ?????????")
                        ),
                        requestFields(
                                fieldWithPath("content").description("?????? ??????")
                        ),
                        responseHeaders(
                                headerWithName("Location").description("redirect url")
                        )
                ));

    }

    @DisplayName("????????? ?????? ?????? ???????????? ????????? ????????? ????????? ?????? 401????????? ????????????.")
    @Test
    public void Board_Write_UnAuthorized() throws Exception {
        BoardPost boardPost = BoardPost.builder().title("??????").content("??????").build();

        mockMvc.perform(post("/board")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boardPost)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andDo(document("Board Write UnAuthorized",
                        requestFields(
                                fieldWithPath("title").description("????????? ??????"),
                                fieldWithPath("content").description("????????? ??????")
                        )
                ));
    }

    @DisplayName("????????? ?????? ?????? ???????????? ????????? ????????? ????????????.")
    @Test
    public void Board_GET_UnAuthorized() throws Exception {
        mockMvc.perform(get("/board/{id}",1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board ??????????????? ?????? ????????? ??????",
                        pathParameters(
                                parameterWithName("id").description("????????? ?????????")
                        ),
                        responseFieldData(
                                boardResponseField(),
                                commentListResponseField()
                        )
                ));
    }

    @DisplayName("????????? ???????????? ??????")
    @Test
    public void Board_Find_By_Id() throws Exception {
        mockMvc.perform(get("/board/{id}",1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board ???????????? ??????",
                    pathParameters(
                            parameterWithName("id").description("????????? ?????????")
                    ),
                    responseFields(
                            fieldWithPath("status").description("Http ????????????"),
                            fieldWithPath("message").description("?????? ?????????"),
                            fieldWithPath("data.id").description("????????? ?????????"),
                            fieldWithPath("data.title").description("????????? ??????"),
                            fieldWithPath("data.content").description("????????? ??????"),
                            fieldWithPath("data.writeTime").description("????????? ?????? ??????"),
                            fieldWithPath("data.commentList.[]").description("????????? ?????? ?????????"),
                            fieldWithPath("data.commentList.[].id").description("?????? ?????????"),
                            fieldWithPath("data.commentList.[].content").description("?????? ??????"),
                            fieldWithPath("data.commentList.[].writeTime").description("?????? ?????? ??????"),
                            fieldWithPath("data.commentList.[].writerLoginId").description("?????? ?????? ?????? ????????? ?????????"),
                            fieldWithPath("data.commentList.[].subCommentList.[]").description("????????? ?????????"),
                            fieldWithPath("data.commentList.[].subCommentList.[].id").description("????????? ?????????"),
                            fieldWithPath("data.commentList.[].subCommentList.[].content").description("????????? ??????"),
                            fieldWithPath("data.commentList.[].subCommentList.[].writerLoginId").description("????????? ?????? ?????? ????????? ?????????"),
                            fieldWithPath("data.commentList.[].subCommentList.[].writeTime").description("????????? ?????? ??????"),
                            fieldWithPath("data.writer.id").description("?????? ?????????"),
                            fieldWithPath("data.writer.writerLoginId").description("?????? ????????? ?????????"),
                            fieldWithPath("data.writer.name").description("?????? ??????")
                    )
                ));
    }

    @DisplayName("????????? ???????????? ?????? ???????????? ????????? 404?????? ??????")
    @Test
    public void Board_Find_By_Id_No_Exists() throws Exception {
        mockMvc.perform(get("/board/{id}",Long.MAX_VALUE))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andDo(document("Board ???????????? ?????? ???????????? ??????",
                        pathParameters(
                                parameterWithName("id").description("????????? ?????????")
                        )
                ));
    }

    @DisplayName("????????? ?????? ??????")
    @Test
    public void Board_Find_All() throws Exception {
        mockMvc.perform(get("/board"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board ?????? ??????",
                        responseDataListField(
                                boardResponseField()
                        )
                ));
    }

    @DisplayName("????????? ???????????? ?????? ??????")
    @Test
    public void Board_Find_All_Preview() throws Exception {
        mockMvc.perform(get("/board/preview"))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(document("Board ???????????? ?????? ??????",
                        responseDataListField(
                            boardPreviewResponseField()
                        )
                ));
    }

    private MockHttpSession setSession(User user) {
        MockHttpSession mockHttpSession = new MockHttpSession(servletContext);
        mockHttpSession.setAttribute(GlobalConst.loginSessionAttributeName,user.getLoginId());
        return mockHttpSession;
    }
}