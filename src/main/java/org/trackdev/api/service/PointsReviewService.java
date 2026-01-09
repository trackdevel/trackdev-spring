package org.trackdev.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.trackdev.api.configuration.UserType;
import org.trackdev.api.controller.exceptions.ServiceException;
import org.trackdev.api.entity.PointsReview;
import org.trackdev.api.entity.Task;
import org.trackdev.api.entity.User;
import org.trackdev.api.repository.PointsReviewRepository;
import org.trackdev.api.utils.ErrorConstants;

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
            throw new ServiceException(ErrorConstants.TASK_ALREADY_REVIEWED);
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
