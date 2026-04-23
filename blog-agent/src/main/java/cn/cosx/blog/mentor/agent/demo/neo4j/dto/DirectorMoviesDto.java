package cn.cosx.blog.mentor.agent.demo.neo4j.dto;

import java.util.List;

public class DirectorMoviesDto {
    private String director;
    private List<String> otherMovies;

    public DirectorMoviesDto() {
    }

    public DirectorMoviesDto(String director, List<String> otherMovies) {
        this.director = director;
        this.otherMovies = otherMovies;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public List<String> getOtherMovies() {
        return otherMovies;
    }

    public void setOtherMovies(List<String> otherMovies) {
        this.otherMovies = otherMovies;
    }
}
