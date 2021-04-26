package com.example.blog.service;

import com.example.blog.dao.BlogRepository;
import com.example.blog.dao.TagRepository;
import com.example.blog.exception.NotFoundException;
import com.example.blog.po.Blog;
import com.example.blog.po.Comment;
import com.example.blog.po.Tag;
import com.example.blog.po.Type;
import com.example.blog.util.MarkdownUtils;
import com.example.blog.util.MyBeanUtils;
import com.example.blog.vo.BlogQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BlogServiceImpl implements BlogService{

    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private TagRepository tagRepository;

    @Override
    @Transactional
    public Blog getBlog(Long id) {
        Optional<Blog> one = blogRepository.findById(id);
        return one.orElse(null);
    }

    @Override
    @Transactional
    public Blog getAndConvert(Long id) {
        Optional<Blog> one = blogRepository.findById(id);
        if (!one.isPresent()) {
            throw new NotFoundException("博客不存在");
        }
        Blog blog = one.get();
        Blog b = new Blog();
        BeanUtils.copyProperties(blog, b); // 隔离数据库

        String content = b.getContent();
        b.setContent(MarkdownUtils.markdownToHtmlExtensions(content));
        blogRepository.updateViews(id);
        return b;
    }

    @Override
    @Transactional
    public Page<Blog> listBlog(Pageable pageable, BlogQuery blog) {
        return blogRepository.findAll(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                if (!"".equals(blog.getTitle()) && blog.getTitle() != null) {
                    predicates.add(cb.like(root.<String>get("title"), "%"+blog.getTitle()+"%"));
                }
                if (blog.getTypeId() != null) {
                    predicates.add(cb.equal(root.<Type>get("type").get("id"), blog.getTypeId()));
                }
                if (blog.isRecommend()) {
                    predicates.add(cb.equal(root.<Boolean>get("recommend"), blog.isRecommend()));
                }
                cq.where(predicates.toArray(new Predicate[predicates.size()]));
                return null;
            }
        },pageable);
    }

    @Override
    @Transactional
    public Page<Blog> listBlog(String query, Pageable pageable) {
        return blogRepository.findByQuery(query, pageable);
    }

    @Override
    @Transactional
    public List<Blog> listRecommendBlogTop(Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");
        Pageable pageable = PageRequest.of(0, size, sort);
        return blogRepository.findTop(pageable);
    }

    @Override
    @Transactional
    public Page<Blog> listBlog(Pageable pageable) {
        return blogRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public Blog saveBlog(Blog blog) {
        if (blog.getId() == null) {
            blog.setCreateTime(new Date());
            blog.setUpdateTime(new Date());
            blog.setViews(0);
        } else {
            blog.setUpdateTime(new Date());
        }
        return blogRepository.save(blog);
    }

    @Transactional
    @Override
    public Blog updateBlog(Long id, Blog blog) {
        Optional<Blog> one = blogRepository.findById(id);
        if (!one.isPresent()) {
            throw new NotFoundException("该博客不存在");
        }
        Blog b = one.get();
        BeanUtils.copyProperties(blog,b, MyBeanUtils.getNullPropertyNames(blog)); // 只拷贝非空的属性
        b.setUpdateTime(new Date());
        return blogRepository.save(b);
    }

    @Transactional
    @Override
    public void deleteBlog(Long id) {
        Blog b = getBlog(id);
        if (b == null) {
            throw new NotFoundException("该博客不存在");
        }
        List<Tag> tags = b.getTags();
        for (Tag tag : tags) {
            List<Blog> blogs = tag.getBlogs();
            blogs.remove(b);
        }

        b.getType().getBlogs().remove(b);
        b.getUser().getBlogs().remove(b);
        System.out.println("博客-删除！");
        blogRepository.deleteById(id);
    }
}