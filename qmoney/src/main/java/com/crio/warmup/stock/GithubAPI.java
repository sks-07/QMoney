package com.crio.warmup.stock;

import org.springframework.web.client.RestTemplate;

class GithubPojo {

    int id;

    public int getId() {

        return id;

    }

    public void setId(int id) {

        this.id = id;

    }

}

class GithubAPI {

    public static void main(String args[]) {

        GithubPojo gitPojo = new RestTemplate().getForObject("https://api.github.com/users/crio-do", GithubPojo.class);

        System.out.println(gitPojo.getId());

    }

}
