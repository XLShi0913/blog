package com.example.blog.service;

import com.example.blog.dao.TypeRepository;
import com.example.blog.exception.NotFoundException;
import com.example.blog.po.Blog;
import com.example.blog.po.Type;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class TypeServiceImpl implements TypeService{

    @Autowired
    private TypeRepository typeRepository;

    @Transactional // 事务，增（增删改查）
    @Override
    public Type saveType(Type type) {
        return typeRepository.save(type);
    }

    @Transactional // 查询单个
    @Override
    public Type getType(Long id) {
        Optional<Type> one = typeRepository.findById(id);
        return one.orElse(null);
    }

    @Transactional // 分页查询
    @Override
    public Page<Type> listType(Pageable pageable) {
        return typeRepository.findAll(pageable);
    }

    @Transactional
    @Override
    public List<Type> listType() {
        return typeRepository.findAll();
    }

    @Override
    @Transactional
    public List<Type> listTypeTop(Integer size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "blogs.size");
        Pageable pageable = PageRequest.of(0, size, sort);
        return typeRepository.findTop(pageable);
    }

    @Transactional // 改
    @Override
    public Type updateType(Long id, Type type) {
        Optional<Type> one = typeRepository.findById(id);
        if (one.isPresent()) {
            Type t = one.get();
            if (!t.getName().equals("无分类")) {
                BeanUtils.copyProperties(type, t);
            } // 不可修改默认分类名称
            return typeRepository.save(t);
        } else {
            throw new NotFoundException("不存在该类型");
        }
    }

    @Transactional // 删
    @Override
    public void deleteType(Long id) {
        Type t = getType(id);
        if (t.getName().equals("无分类")) return; // 无法删除默认分类
        Type defaultType = getTypeByName("无分类");
        List<Blog> blogs = t.getBlogs();
        for (Blog b : blogs) {
            b.setType(defaultType);
        } // 删除一个分类时，将下面的博客类型改成默认分类类型“无分类”
        typeRepository.deleteById(id);
    }

    @Override
    public Type getTypeByName(String name) {
        return typeRepository.findByName(name);
    }
}
