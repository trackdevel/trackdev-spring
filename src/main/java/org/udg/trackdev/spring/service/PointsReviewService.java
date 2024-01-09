package org.udg.trackdev.spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.trackdev.spring.configuration.UserType;
import org.udg.trackdev.spring.controller.exceptions.ServiceException;
import org.udg.trackdev.spring.entity.PointsReview;
import org.udg.trackdev.spring.entity.Task;
import org.udg.trackdev.spring.entity.User;
import org.udg.trackdev.spring.repository.PointsReviewRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PointsReviewService extends BaseServiceLong<PointsReview, PointsReviewRepository> {

    @Autowired
    UserService userService;

    public List<PointsReview> getPointsReview(String userId){
        User user = userService.get(userId);
        List<PointsReview> pointsReviews = repo.findAll();
        if (user.isUserType(UserType.ADMIN)){
            return pointsReviews;
        } else{
            return pointsReviews.stream().filter(pointsReview -> pointsReview.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());
        }
    }

    @Transactional
    public ResponseEntity addPointsReview(Integer points, String comment, User user, Task task){
        if(task.getPointsReviewList().stream().filter(pointsReview1 -> pointsReview1.getUser().getId().equals(user.getId())).count() > 1){
            throw new ServiceException("This user has already reviewed this task");
        }
        else if(task.getPointsReviewList().stream().filter(pointsReview1 -> pointsReview1.getUser().getId().equals(user.getId())).count() == 1){
            Optional<PointsReview> review = task.getPointsReviewList().stream().filter(pointsReview1 -> pointsReview1.getUser().getId()
                    .equals(user.getId())).findFirst();
            if(review.isPresent()){
                PointsReview pointsReview = review.get();
                pointsReview.setPoints(points);
                pointsReview.setComment(comment);
                repo.save(pointsReview);
            }

        }
        else{
            PointsReview pointsReview = new PointsReview(points, comment, task, user);
            task.addPointsReview(pointsReview);
            user.addPointsReview(pointsReview);
            repo.save(pointsReview);
        }
        return ResponseEntity.ok().build();
    }

}
