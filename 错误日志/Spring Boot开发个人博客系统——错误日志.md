# Spring Boot开发个人博客系统——错误日志

> 讲师：李仁密；视频地址：BV1LV411C7sS(b站)；本人联系方式：2722316138@qq.com

本项目是我接触开发框架的第一个项目，在此之前我根本不知道什么是Spring Boot、thymeleaf、MySQL、JPA、hibernate，可以说是纯小白了。究其根本还是为了找实习（卑微的打工人）才做的这个项目。做到现在，个人感觉这个项目面对新人还是比较友好的吧，在项目过程中遇到的种种问题，皆记录于下文。希望兄弟们遇到新问题时不要气馁，毕竟解决问题的过程就是学习的过程。
感谢B站UP主 编程不止Coding 提供的视频教程和源码。


## 1、新版本Spring Boot指定 thymeleaf 版本的标签

> 2021年4月16日，晨

源项目中的Spring Boot版本为1.5.7.RELEASE，其thymeleaf版本指定的标签是thymeleaf.version，如下：

```xml
<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<thymeleaf.version>3.0.2.RELEASE</thymeleaf.version>
		<thymeleaf-layout-dialect.version>2.1.1</thymeleaf-layout-dialect.version>
</properties>
```

我使用的Spring Boot版本是2.4.4，指定thymeleaf版本的标签应该用springboot-thymeleaf.version，如下

```xml
<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<springboot-thymeleaf.version>3.0.2.RELEASE</springboot-thymeleaf.version>
		<thymeleaf-layout-dialect.version>2.1.1</thymeleaf-layout-dialect.version>
</properties>
```



## 2、创建数据库时，注意application.yml中的数据库设置

> 2021年4月16日，夜

这个地方，视频中使用的设置是运行环境，其中对hibernate的设置：

```yml
jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    open-in-view: false
```

我用的设置是

```yml
jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    open-in-view: false
```

因为把数据库结构设置成了不可变，所以建不了数据库*~~（我是傻逼）~~*



## 3、使用Hibernate操作数据库过程中出现的错误

> 2021年4月22日，凌晨

这个问题困扰了我两天，按照教程中的实体关联设置以及保存/更新/删除操作，我在save、update和delete时都出现了异常，至4月21日夜，其中具体的原因我也不能说全搞明白了，解决方法也是很原始、效率很低的一种。暂且做下记录，今后若是有新的理解，再行更新。
~~*为了尽快做出来项目，我在寝室自闭了五六天了，这两天被这个东西搞得心态大崩，还被小老板催着干活，女朋友也在抱怨我不关心她（其实今天拿到了女朋友买的机械键盘，女朋友真香）。生活啊，压垮了我这个菜鸡的腰。*~~
首先是预备知识，大佬可跳过，详细了解可在论坛中自行查找，解决问题下面说的就够了。你要是问我为什么李老师的测试都通过了而我的测试没通过，那我也不知道。要是我的测试一把就通过了我现在甚至可能还不知道原来JPA下边还有一层Hibernate实现。

### 预备知识一：什么是JPA、Hibernate和MySQL

> JPA：将Java Bean固化存储到硬盘上的接口（规范）
> 
> 关系型数据库：Java对象中可能有其他对象的引用，保存对象就要保存这些引用。这些引用称为关系，保存这些关系的数据库就是关系型数据库啦。项	目中使用的MySQL就是关系型数据库的一种。
> 
> Hibernate：JPA的实现，可以通过面向对象方法生成SQL语句。程序员只需要根据JPA的规范使用现有数据库操作方法或根据规则进行组合，Hibernate可以自动生成SQL语句，避免了面向数据库编程的困境，提高了生产力（也给我这种菜鸡制造了数不清的麻烦）

我们在项目中直接打交道的是JPA。程序员负责定义JPA，也就是dao层的那些继承于JpaRepository的接口，这些接口的实现由Hibernate完成，实现的过程中Hibernate会自动生成SQL语句帮助我们操作数据库。

### 预备知识二：Hibernate中实体类的三种状态

实体类可能存在的位置：

> JVM内存：众所周知，new出来的对象在堆中
> Session：服务器为维护HTTP连接而开辟出的一块内存空间
> 数据库：实体类已经被固化到硬盘上

三种状态：

>| 状态                   | 是否存在于JVM | 是否存在于Session | 是否存在于数据库 |
| :--------------------- | ------------- | ----------------- | ---------------- |
| Transient(临时状态)    | 是            | 否                | 否               |
| Persistent(持久化状态) | 是            | 是                | 是               |
| Detached(游离状态)     | 是            | 否                | 是               |

### 预备知识三：Hibernate懒加载

> 懒加载：在查询某一条数据的时候并不会直接将这条数据以指定对象的形式来返回，而是在你真正需要使用该对象里面的一些属性的时候才会去数据库访问并得到数据。懒加载真正拿到的是一个代理对象，访问对象属性是通过代理调用setter和getter方法实现的。
> 
> 立即加载：与懒加载相反，请求数据的时候会把对象整体加载到session中。

JPA中的设置方法：在注解中定义fetch属性

### 预备知识四：JPA中的Cascade Type

下面的解释中，规定在一对关系中，有cascade注解的类为Parent，没有cascade注解的类为Child

> CascadeType.PERSIST：若在Parent的Set<Child>中新增了t_child中没有的Child对象，则这个新的Child对象也会被保存到数据表中；
> CascadeType.REMOVE：若要删除一个Parent对象，其Set<Child>中所有的Child会从t_child中删除；
> CascadeType.MERGE：若在Parent中调用Set<Child>，对集合中的Child对象进行了属性修改，则这些Child对象会在t_child表中进行更新；
> CascadeType.REFRESH：用不到，暂时不管

可以参考链接：http://westerly-lzh.github.io/cn/2014/12/JPA-CascadeType-Explaining/

### 项目中实体类的关联重现

原项目中的实体类关联的定义如下（先只讨论Blog - Type - Tag的关系）：

> Blog - Tag是many-to-many，由Blog维护，Blog中的Tag使用级联新增，默认加载方式两边都为FetchType.LAZY；
> Blog - Type是many-to-one，由Blog维护，默认加载方式在Blog端为FetchType.EAGER，在Type端为FetchType.LAZY。

```java
@Entity
@Table(name = "t_blog")
public class Blog {
	// 省略的field...
	@ManyToOne
    private Type type;
    @ManyToMany(cascade = {CascadeType.PERSIST}) // 标记1
    private List<Tag> tags = new ArrayList<>();
    // 省略的getter/setter方法...
}
```
```java
@Entity
@Table(name = "t_tag")
public class Tag {
	// 省略的field...
	@ManyToMany(mappedBy = "tags")
    private List<Blog> blogs = new ArrayList<>();
    // 省略的getter/setter方法...
}
```
```java
@Entity
@Table(name = "t_type")
public class Type {
	// 省略的field...
	@OneToMany(mappedBy = "type")
    private List<Blog> blogs = new ArrayList<>();
    // 省略的getter/setter方法...
}
```

### 因懒加载引发的异常分析

如果你跟李老师写的一样，你根本进不去博客编辑页面*~~（我也不知道为什么李老师能通过测试）~~*，你的浏览器会跳转到未知错误页面，如下：
*~~我自己写的错误页面为什么我自己这么讨厌呢~~*

<img src="..\错误日志\错误页面1.png" style="zoom:70%;" />

同时你的控制台会出现如下的异常：

```
2021-04-22 01:23:45.166 ERROR 7688 --- [nio-8081-exec-4] c.e.b.h.ControllerExceptionHandler       : Requst URL : /admin/blogs/56/input, Exception : {}
org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.example.blog.po.Blog.tags, could not initialize proxy - no Session
... // 省略的调用栈
```

大概意思是：无法初始化Blog类中的的tags集合 -> 因为无法初始化代理对象 -> 因为没有session

让我们从源头一步步找起，首先，新增博客的按钮是这样滴：

```html
<a href="#" th:href="@{/admin/blogs/input}" class="ui mini right floated teal basic button">新增</a>
```

BlogController对这一请求的响应是这样滴：

```java
@GetMapping("/blogs/input")
public String input(Model model) {
	model.addAttribute("types", typeService.listType());
	model.addAttribute("tags", tagService.listTag());
	model.addAttribute("blog", new Blog()); // 标记
	return "admin/blogs-input";
}
```

注意这里懒加载失败的tags是Blog对象中的，是标记处的new出来的Blog对象中的一个属性。因为tags属性使用的是懒加载，这里给model传递的new Blog()对象中的tags实际上是一个代理对象proxy，而不是一个new ArrayList<>()。此时JVM内存中的状态可以这样理解：

> model（ blog（ 数据 数据 对象 对象 proxy ） ） 这些数据和对象是立即加载的属性，而这个proxy就是tags懒加载上的属性

至此还没有问题。

接下来发生了页面跳转，admin/blogs的session关闭（缓存清空），服务器针对新页面 admin/blogs-input 新建了一个session。立即加载的属性还在blog里边，而tags属性是一个代理，并且这个代理是定义在上一个session中的。所以异常码中的 no session 指的是 admin/blogs 的session，也就是定义proxy对象的session，它已经关闭了，所以proxy初始化失败了，所以tags初始化失败了。用术语说，tags属性现在是游离状态，在session中找不到。

博客编辑出现的错跟上边一样，编辑按钮的响应也会传递给model一个Blog对象，这里就不说了。

关于问题的解决，网上的解决方法大概有两种：其一，可以通过修改属性控制session关闭的时机；其二，把懒加载改成立即加载。因为我是菜鸡，不知道第一种怎么搞，所以我选择方法二。

### 因关联关系引发的异常分析

由标记1处 cascade = {CascadeType.PERSIST} 引发的错误，由博客新增进入admin/blogs-input 后，在页面按下保存/发布时，浏览器的错误页面跟上个错误是一样的，控制台出现以下异常：*~~（我讨厌那个页面，不想放图了）~~*

```xml
org.springframework.dao.InvalidDataAccessApiUsageException: detached entity passed to persist: com.example.blog.po.Tag; nested exception is org.hibernate.PersistentObjectException: detached entity passed to persist: com.example.blog.po.Tag
... // 省略的调用栈
Caused by: org.hibernate.PersistentObjectException: detached entity passed to persist: com.example.blog.po.Tag
... // 省略的调用栈
```

好，那么又是这个Blog属性tags

博客新增保存了一个新的Blog对象，博客对象内的属性tags按照规则Cascade.PERSIST进行保存。回顾一下Cascade.PERSIST：若在Parent的Set<Child>中新增了t_child中没有的Child对象，则这个新的Child对象也会被保存到数据表中。我们在博客编辑页面上选择的标签集合，是一个字符串类型的tagsId，可以通过查找转换成一个List<Tag>。Cascade.PERSIST有这样的规则：如果新增的Tag已经存在在数据库，会报错。（这里我的理解也只到这，希望大佬指正）

注意到，在业务上对Tag的增删改是有专门的页面进行操作的，博客编辑只能在现有的Tag集合中进行选择，因此我们不需要这个级联新增，直接删掉就行。

### 删除失效的异常分析

至此，博客编辑和博客新增的问题就已经排查完了，视频还剩下最后几分钟。当我开心的实现完李老师口中那个简单的删除控制后，以为大功告成的时候，问题出现了。我惊奇的发现删除不起作用了。此时，我这次的debug之路才过去不到一个下午，还有20号晚上、21号一整天的痛苦debug经历，以及22号凌晨将近3个小时+早晨1个多小时的日志记录经历尚未度过，就像1941年6月的苏联，还不知道接下来要经历什么样的苦痛。*~~苏联的各所学校刚在前一天举办了离校派对，..., 伟大的卫国战争开始了~~*

所以问题是，对一篇博客点了删除之后，它并没有从数据库中真正删除掉。Hibernate生成的SQL语句如下：

```sql
Hibernate: select type0_.id as id1_4_, type0_.name as name2_4_ from t_type type0_
Hibernate: select blog0_.id as id1_0_, ...
... // 下边都是select语句
```

虽然我是菜鸡，但是我大概知道，select是选择的意思，delete才是删除。所以这个过程中只生成了查找语句，并没有删除语句。

那么我们来debug一下吧，看看是不是没有找到这个blog对象，还是删除操作出了问题。调试程序和断点设置如下：

```java
@Transactional
@Override
public void deleteBlog(Long id) {
	Blog b = getBlog(id); // 断点1
	System.out.println("博客-删除！");
	blogRepository.deleteById(id);
    b = getBlog(id); // 断点2
}
// 下边是调用的getBlog方法
// 现版本Jparepository的findNyId()方法就是视频中的findOne()方法，用法就是下边的用法，这个问题很好解决，就没单独拎出来说
public Blog getBlog(Long id) {
	Optional<Blog> one = blogRepository.findById(id);
	return one.orElse(null);
}
```

调试的结果：在断点1处成功的查询到了对应id的Blog对象，在断点2处查询得到的是null。就应该是这样呀，不是删掉了吗，为啥数据库里边还有呢？

根据基本常识，要对硬盘中的数据段进行处理，需要把它加载到内存中，放到项目中说，数据经历的是一个 数据库 -> 内存 -> 数据库 的过程。我们在blogs页面上看到的那些博客对象，是session缓存中的对象。JVM是一个车床，我们要操作一个零件，自然要把这个零件放到操作平台上，也就是JVM内存中，也就是数据库-> 内存的过程*~~（机械生的执念）~~*；操作完毕后，数据库会检查这个零件跟操作前的区别，并在数据库中同步这个区别，即内存 - > 数据库的过程。同步的操作就是Hibernate生成的SQL语句。

调试结果表明我们在内存中的操作成功了，但是根据生成的SQL语句，我们在数据库同步的过程中并没有删掉博客对象b，也就是说直到同步的那一刻，JVM内存中还是有博客b这个对象的。这一现象可以作如下理解：

> 当我们点击删除的时候，JVM中运行程序，找到指定id的博客对象b，将他加载到JVM内存，做一个删除标记。注意这里堆内存上的数据并没有直接被清空，真正的清空要等GC来进行，而我们程序员并不知道GC执行的时机；

> 因为我们采用的是立即加载模式，对象b里边的type、tags属性也都加载到了内存，而这些属性持有b的引用。也就是说，指向b的引用并不为0，所以GC不会回收对象b；所以同步到数据库和session中时，对象b是存在的，因而删除失败了。

从网上查到的解决办法还是有两种：其一，使用懒加载，只要我不加载属性到内存，内存就可以正确同步，因为我们前面用立即加载处理了游离状态的问题，所以我没法用这种方法，其正确性也有待验证；其二，我直接把所有指向blog的引用手动删除，简单暴力，我是菜鸡，实在试不出来别的方法了，就这么着把。

（注意，这里删除失败的问题跟游离状态没什么关系，懒加载才是因为待加载对象处于游离状态而产生的bug）

### 总结：要修改的代码

更改Blog和Tag类中的加载模式和级联关系，如下：

```java
@Entity
@Table(name = "t_blog")
public class Blog {
	// 省略的field...
	@ManyToOne
    private Type type;
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Tag> tags = new ArrayList<>();
    // 省略的getter/setter方法...
}
```
```java
@Entity
@Table(name = "t_tag")
public class Tag {
	// 省略的field...
	@ManyToMany(mappedBy = "tags", fetch = FetchType.EAGER)
    private List<Blog> blogs = new ArrayList<>();
    // 省略的getter/setter方法...
}
```
在BlogService的删除事务中显式的删除关联，如下：

```java
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
        blogRepository.deleteById(id);
}
```

## 4、新的Sort和PageRequest对象的使用

在前端页面展示的业务模块中，需要挑选出下属博客最多的几个Type对象，在其服务TypeServiceImp中使用到了这两个类。视频中的使用方法如下：

```java
@Override
@Transactional
public List<Type> listTypeTop(Integer size) {
	Sort sort = new Sort(Sort.Direction.DESC, "blogs.size");
	Pageable pageable = new PageRequest(0, size, sort);
	return typeRepository.findTop(pageable);
}
```

我的版本中改成了如下方式：

```java
@Override
@Transactional
public List<Type> listTypeTop(Integer size) {
	Sort sort = Sort.by(Sort.Direction.DESC, "blogs.size");
	Pageable pageable = PageRequest.of(0, size, sort);
	return typeRepository.findTop(pageable);
}
```

根据提示得知，Sort对象改成了private权限，PageRequest改成了protect权限，都无法直接访问。要通过新定义的接口方法得到相应的对象。这是在封装方面的一个进步。

## 5、博客评论两级化的业务逻辑

博客评论两级化，就是只显示两级的博客评论，b站的评论区就是这样的一种形式。这里李老师写在CommentService里边的业务逻辑有点难理解，我修改并注释了一下，修改后如下：

```java
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
```

