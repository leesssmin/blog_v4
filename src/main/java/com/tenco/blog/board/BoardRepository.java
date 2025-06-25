package com.tenco.blog.board;

import com.tenco.blog._core.errors.exception.Exception404;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor // 생성자 자동 생성 + 멤버 변수 -> DI 처리 됨
@Repository
public class BoardRepository {

    private static final Logger log = LoggerFactory.getLogger(BoardRepository.class);
    private final EntityManager em;

    // 게시글 수정하기 - 더티 체킹 활용
    @Transactional
    public Board updateById(Long id, BoardRequest.UpdateDTO reqDTO) {
        log.info("게시글 수정 시작 - ID : {}", id);
        Board board = findById(id);
        board.setTitle(reqDTO.getTitle());
        board.setContent(reqDTO.getContent());
        return board;
    }

    // 게시글 삭제
    @Transactional
    public void deleteById(Long id) {
        log.info("게시글 삭제 시작 - ID : {}", id);
        String jpql = " DELETE FROM Board b WHERE b.id = :id ";
        Query query = em.createQuery(jpql);
        query.setParameter("id", id);

        int deletedCount = query.executeUpdate(); // I, U, D
        if(deletedCount == 0) {
            throw new Exception404("삭제할 게시글이 없습니다");
        }
        log.info("게시글 삭제 완료 - 삭제 행 수: {}", deletedCount);
    }

    @Transactional
    public void deleteByIdSafely(Long id) {
        // 영속성 컨텍스트를 활용한 삭제 처리
        // 1. 먼저 삭제할 엔티티를 영속 상태로 조회
        Board board = em.find(Board.class, id);
        // board -> 영속화 됨
        // 2. 엔티티 존재 여부 확인
        if(board == null) {
            throw new Exception404("삭제할 게시글이 없습니다");
        }

        // 3. 영속화 상태의 엔티티를 삭제 상태로 변경
        em.remove(board);
        // 1차 캐시에서 자동 제거
        // 연관관계 처리도 자동 수행 (캐스케이드)
    }


    /**
     * 게시글 저장 : User와 연관관계를 가진 Board 엔티티 영속화
     * @param board
     * @return
     */
    @Transactional
    public Board save(Board board) {
        log.info("게시글 저장 시작 - 제목 :  {}, 작성자 : {}",
                board.getTitle(), board.getUser().getUsername());
        em.persist(board);
         // board - user
        // 이 후 시점에는 사실 같은 메모리주소를 가리킨다.
        return board;
    }

    /**
     * 전체 게시글 조회
     */
    public List<Board> findByAll() {
        log.info("전체 게시글 조회 시작");
        String jqpl = " SELECT b FROM Board b ORDER BY b.id DESC ";
        TypedQuery query = em.createQuery(jqpl, Board.class);
        List<Board> boardList = query.getResultList();
        return boardList;
    }

    /**
     * 게시글 단건 조회 (PK 기준)
     * @param id : Board 엔티티에 ID 값
     * @return : Board 엔티티
     */
    public Board findById(Long id) {
        log.info("게시글 단건 조회");
        // 조회 - PK 조회는 무조건 엔티티 매니저에 메서드 활용이 이득이다.
        Board board = em.find(Board.class, id);
        return board;
    }

}

