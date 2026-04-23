package cn.cosx.blog.mentor.agent.demo.neo4j.service;

import cn.cosx.blog.mentor.agent.demo.neo4j.dto.DirectorMoviesDto;
import cn.cosx.blog.mentor.agent.demo.neo4j.repository.MovieGraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GraphService {

    @Autowired
    private MovieGraphRepository repository;

    public String retrieveContext(String movieName) {
        List<DirectorMoviesDto> results = repository.findOtherMoviesBySameDirector(movieName);

        if (results.isEmpty()) {
            return "未找到导演过《" + movieName + "》的导演的其他作品。";
        }

        StringBuilder sb = new StringBuilder();
        for (DirectorMoviesDto row : results) {
            String director = row.getDirector();
            @SuppressWarnings("unchecked")
            List<String> movies = row.getOtherMovies();
            sb.append(String.format("- 导演 %s 还执导了：%s\n", director, String.join("、", movies)));
        }
        return sb.toString().trim();

    }

}
