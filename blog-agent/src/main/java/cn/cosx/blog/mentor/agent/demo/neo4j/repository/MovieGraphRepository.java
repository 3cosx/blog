package cn.cosx.blog.mentor.agent.demo.neo4j.repository;

import cn.cosx.blog.mentor.agent.demo.neo4j.dto.DirectorMoviesDto;
import cn.cosx.blog.mentor.agent.demo.neo4j.entity.Movie;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGraphRepository extends Neo4jRepository<Movie, String> {

    @Query("""
            MATCH (m:Movie {title: $title}) <-[:DIRECTED]- (d:Director) -[:DIRECTED]-> (other:Movie)
            WHERE other.title <> $title
            RETURN d.name AS director, collect(other.title + ' (' + other.year + ')') AS otherMovies
            """)
    List<DirectorMoviesDto> findOtherMoviesBySameDirector(String title);
    @Query("""
            MATCH (m:Movie {title: $title} <-[:DIRECTED]- (d:Director) -[:DIRECTED]-> (other:Movie)
            WHERE other.title <> $title
            RETURN d.name AS director, collect(other.title + ' (' + other.year + ')') AS otherDirector
            """)
    List<DirectorMoviesDto> findOtherMoviesBySameDirector1(String title);

}
