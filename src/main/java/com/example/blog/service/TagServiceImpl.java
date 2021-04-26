package com.example.blog.service;

import com.example.blog.dao.TagRepository;
import com.example.blog.exception.NotFoundException;
import com.example.blog.po.Blog;
import com.example.blog.po.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService{
    @Autowired
    private TagRepository tagRepository;

    @Override
    @Transactional
    public Tag saveTag(Tag tag) {
        return tagRepository.save(tag);
    }

    @Override
    @Transactional
    public Tag getTag(Long id) {
        Optional<Tag> one = tagRepository.findById(id);
        return one.orElse(null);
    }

    @Override
    @Transactional
    public Page<Tag> listTag(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Tag updateTag(Long id, Tag tag) {
        Optional<Tag> one = tagRepository.findById(id);
        if (one.isPresent()) {
            Tag tag1 = one.get();
            BeanUtils.copyProperties(tag, tag1);
            return tagRepository.save(tag1);
        }else {
            throw new NotFoundException("该标签不存在");
        }
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        Tag t = getTag(id);
        List<Blog> blogs = t.getBlogs();
        for (Blog b : blogs) {
            b.getTags().remove(t);
        }
        tagRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name);
    }

    @Override
    @Transactional
    public List<Tag> listTag() {
        return tagRepository.findAll();
    }

    @Override
    @Transactional
    public List<Tag> listTag(String ids) {
        List<Long> list = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            String[] idArr = ids.split(",");
            for (String s : idArr) {
                list.add(Long.valueOf(s));
            }
        }
        return tagRepository.findAllById(list);
    }

    @Override
    @Transactional
    public List<Tag> listTagTop(Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "blogs.size");
        Pageable pageable = PageRequest.of(0, size, sort);
        return tagRepository.findTop(pageable);
    }
}
