package cn.cosx.blog.auth.param;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class NoticeParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
}
