package com.example.blog.service;

import com.example.blog.dao.CommentRepository;
import com.example.blog.po.Comment;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService{

    @Autowired
    private CommentRepository commentRepository;

    @Override
    @Transactional
    public List<Comment> listCommentByBlogId(Long blogId) {
        List<Comment> comments = commentRepository.findByBlogIdAndParentCommentNull(
                blogId, Sort.by(Sort.Direction.ASC, "creatTime"));
        return eachComment(comments);
    }

    @Override
    @Transactional
    public Comment saveComment(Comment comment) {
        Long parentCommentId = comment.getParentComment().getId();
        if (parentCommentId != -1) {
            Optional<Comment> one = commentRepository.findById(parentCommentId);
            Comment parentComment = one.orElse(null);
            comment.setParentComment(parentComment);
        } else {
            comment.setParentComment(null);
        }
        comment.setCreatTime(new Date());
        return commentRepository.save(comment);
    }

    // 将blog的所有评论扁平化成两级评论
    private List<Comment> eachComment(List<Comment> comments) {
        // 从数据库复制并重构一份，修改的属性是新列表中的，而非数据库中的
        List<Comment> commentsView = new ArrayList<>();
        for (Comment comment : comments) {
            Comment c = new Comment();
            BeanUtils.copyProperties(comment,c);
            commentsView.add(c);
        }
        // 合并评论的各层子代到第一级子代集合中
        combineChildren(commentsView);
        return commentsView;
    }

    // 将顶级节点的所有后代偏平化存放到replyComment属性中
    private void combineChildren(List<Comment> comments) {
        for (Comment comment : comments) {
            // 对于每个顶级节点comment，将每个子节点reply及其后代都存放到缓存数组中
            List<Comment> replys = comment.getReplyComments();
            for(Comment reply : replys) {
                recursively(reply);
            }
            // 重定义顶级节点comment的子节点数组，用缓存区覆盖
            comment.setReplyComments(tempReplys);
            // 缓存区重定向
            tempReplys = new ArrayList<>();
        }
    }

    // 缓存数组
    private List<Comment> tempReplys = new ArrayList<>();

    // 递归迭代，将节点comment以及所有的后代节点都加入缓存数组
    private void recursively(Comment comment) {
        tempReplys.add(comment); // 递归过程的实质操作
        List<Comment> replys = comment.getReplyComments();
        if (replys.size() == 0) return; // 递归基
        for (Comment reply : replys) { // 向下递归
            recursively(reply);
        }
    }
}
