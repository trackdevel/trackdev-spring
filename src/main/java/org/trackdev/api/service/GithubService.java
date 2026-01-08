package org.trackdev.api.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.trackdev.api.entity.GithubInfo;
import org.trackdev.api.repository.GithubInfoRepository;
import org.trackdev.api.utils.GithubConstants;

@Service
public class GithubService extends BaseServiceUUID<GithubInfo, GithubInfoRepository>{

    private final RestTemplate restTemplate;

    public GithubService(){
        super();
        this.restTemplate = new RestTemplate();
    }

    public ResponseEntity<GithubInfo> getGithubInformation(String token){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        try{
            return restTemplate.exchange(
                    GithubConstants.GITHUB_API_USER_URL,
                    org.springframework.http.HttpMethod.GET,
                    requestEntity,
                    GithubInfo.class
            );
        }
        catch (HttpClientErrorException e){
            if(e.getStatusCode().value() == 401){
                return ResponseEntity.status(401).build();
            }
            else{
                return ResponseEntity.status(500).build();
            }
        }

    }


}
