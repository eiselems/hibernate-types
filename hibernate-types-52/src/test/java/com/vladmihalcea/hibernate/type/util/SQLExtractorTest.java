package com.vladmihalcea.hibernate.type.util;

import org.junit.Test;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.time.LocalDate;

import static org.junit.Assert.assertNotNull;

/**
 * @author Vlad Mihalcea
 */
public class SQLExtractorTest extends AbstractPostgreSQLIntegrationTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Post.class,
            PostComment.class
        };
    }

    @Test
    public void testJPQL() {
        doInJPA(entityManager -> {
            Query query = entityManager
            .createQuery(
                "select " +
                "   YEAR(p.createdOn) as year, " +
                "   count(p) as postCount " +
                "from " +
                "   Post p " +
                "group by " +
                "   YEAR(p.createdOn)", Tuple.class);

            String sql = SQLExtractor.from(query);
            assertNotNull(sql);
            LOGGER.info("SQL query: {}", sql);
        });
    }

    @Test
    public void testCriteriaAPI() {
        doInJPA(entityManager -> {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();

            CriteriaQuery<PostComment> criteria = builder.createQuery(PostComment.class);

            Root<PostComment> postComment = criteria.from(PostComment.class);
            Join<PostComment, Post> post = postComment.join("post");

            criteria.where(
                builder.like(post.get("title"), "%Java%")
            );

            criteria.orderBy(
                builder.asc(postComment.get("id"))
            );

            Query query = entityManager.createQuery(criteria);

            String sql = SQLExtractor.from(query);
            assertNotNull(sql);
            LOGGER.info("SQL query: {}", sql);
        });
    }

    @Entity(name = "Post")
    @Table(name = "post")
    public static class Post {

        @Id
        private Long id;

        private String title;

        @Column(name = "created_on")
        private LocalDate createdOn;

        public Long getId() {
            return id;
        }

        public Post setId(Long id) {
            this.id = id;
            return this;
        }

        public String getTitle() {
            return title;
        }

        public Post setTitle(String title) {
            this.title = title;
            return this;
        }

        public LocalDate getCreatedOn() {
            return createdOn;
        }

        public Post setCreatedOn(LocalDate createdOn) {
            this.createdOn = createdOn;
            return this;
        }
    }

    @Entity(name = "PostComment")
    @Table(name = "post_comment")
    public static class PostComment {

        @Id
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        private Post post;

        private String review;

        public Long getId() {
            return id;
        }

        public PostComment setId(Long id) {
            this.id = id;
            return this;
        }

        public Post getPost() {
            return post;
        }

        public PostComment setPost(Post post) {
            this.post = post;
            return this;
        }

        public String getReview() {
            return review;
        }

        public PostComment setReview(String review) {
            this.review = review;
            return this;
        }
    }
}
