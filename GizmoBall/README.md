# GizmoBall Lab
## 问题描述
设计并实现弹球游戏（类似三维弹球）
## 需求分析
### 元件种类及特性
- 边界(border)：系统初始化时自动放置，球碰到后会反弹并损失速度
- 方形(rectangle)：可拖拽物体，游戏开始后不可移动，球碰到后会反弹并损失速度
- 圆形(circle)：可拖拽物体，游戏开始后不可移动，球碰到后会反弹并损失速度
- 三角形(triangle)：可拖拽物体，游戏开始后不可移动，球碰到后会反弹并损失速度
- 黑洞(black hole)：可拖拽物体，游戏开始后不可移动，会对球产生吸引力（和距离平方成反比），球碰到后会消失
- 直管道(pipe)：可拖拽物体，游戏开始后不可移动，球碰到管壁会反弹，管道内最低速度30/tick，不受重力，引力，摩擦力影响
- 弯管道(curved pipe)：可拖拽物体，游戏开始后不可移动，球碰到管壁会反弹，管道内最低速度90/tick，不受重力，引力，摩擦力影响
- 左挡板(left flipper)：可拖拽物体，游戏开始后可绕轴旋转，球碰到后会反弹并被传递速度
- 右挡板(right flipper)：可拖拽物体，游戏开始后可绕轴旋转，球碰到后会反弹并被传递速度
- 球(ball)：可拖拽物体，游戏开始后随重力自由落体
### 操作种类
- 添加(add)：拖拽物体到画布上以添加物体，添加时物体不可重叠，否则提示**操作物件失败：物件重叠**
- 选中(select)：单击以选中物体，此时物体会高亮提示
- 删除(delete)：删除选中物体
- 放大(zoom out)：放大选中物体，放大时物体不可重叠，否则提示**操作物件失败：物件重叠**
- 缩小(zoom in)：缩小选中物体，不能将物体缩小到小于一格，否则提示**操作物件失败：物件已经最小**
- 旋转(rotate)：旋转选中物体
- 移动(move)：移动选中物体，移动时物体不可重叠，否则提示**操作物件失败：物件重叠**
- 加载文件(load)：加载存档文件（json）到游戏中，若加载失败则提示**报错信息**
- 保存文件(save)：保存当前游戏信息到存档文件（json）中，若保存失败则提示**报错信息**
- 清空(clear)：清空画布上所有物体
- 游戏(play)：进入游戏模式
- 设计(design)：进入设计模式
### 规则约束
- 游戏模式下禁止除切换模式外的任何操作
- 设计模式切换到游戏模式时自动在本目录下创建名为 `.snapshot.json` 的存档
- 球全消失后自动切换到设计模式
### 快捷键
- W：上移元件
- A：左移元件
- S：下移元件
- D：右移元件
- DEL：删除元件
- SHIFT：左旋元件
- CTRL：右旋元件
- ENTER：模式切换
- <-：左挡板摆动
- ->：右挡板摆动
- F1：调试模式
## 类的职责
### 引擎层
**AbstractWorld**：**AbstractWorld**代表了弹球游戏中所有物体(PhysicsBody)所处的“世界”。这个“世界”中包含了所有物体信息，以及处理碰撞需要的类。可以向这个世界中添加物体、删除物体、获取存在的物体，以及通过tick方法让这个“世界”运行起来。  
**PhysicsBody**：代表游戏中的一个物理实体（球、墙壁、挡板等都属于PhysicsBody），受到物理规则的影响。包含基本的物理属性：质量、速度、受力等，可以根据自身受力、速度信息计算出新的速度以及位置，以及这个物理实体的基础形状(AbstractShape)。  
**AbstractShape**：代表一种抽象的形状，可以是圆形(Circle)、多边形(Polygon)（具体到三角形(Triangle)、矩形(Rectangle)）、半圆(QuarterCircle)等。一个形状可以计算自身的**AABB**，在某个向量上的投影等信息。在实际游戏中，我们假设需要的形状都是凸形(Convex)，所以实现了**Convex**接口。  
**Convex**：凸形接口，接口方法包含了凸形共通的可以计算的信息如，分离轴，焦点，碰撞特征等。  
#### 碰撞检测相关
**CollisionDetector**：负责碰撞检测接口，包含了碰撞检测需要的基础方法，对所有物体进行碰撞检测。因为具体的碰撞检测可能很复杂，实现的复杂程度和检测效率都有所不同，所以采用接口表示。   
**BasicCollisionDetector**：基础的碰撞检测实现类。在经过**CollisionFilter**过滤接受碰撞的物体之后，借助**DetectorUtil**工具类实现对所有物体的碰撞检测并获取碰撞的穿透信息(Penetration)后通过**ManifoldSolver**获取碰撞信息用于解决碰撞。  
**CollisionFilter**：判断两个**PhysicsBody**是否接受碰撞检测的过滤器接口。  
**DetectorUtil**：碰撞检测工具类，可以检测一些基础的碰撞，**AABB**是否重叠等。  
**ManifoldSolver**：根据物体的穿透信息获取碰撞的**Manofold**信息。  
**SequentialImpulses**：在获取碰撞相关的信息之后，根据碰撞的信息更新物体的位置、速度等。  

### 领域层
**GizmoWorld**：**AbstractWorld**的具体化实现，包含了**TickListener**在每一帧时运行的回调函数以及**PhysicsBody**类型的映射信息，用于更好地对各种事件进行处理。  
**TickListener**：游戏每个会触发一次回调函数接口。不同物体处理碰撞可能有不同的结果，比如黑洞与球碰撞球会被移除，与一般障碍物碰撞则不会，这种情况下需要通过**TickListener**接口实现。实现了**TickListener**的**BlackHoleListener**、**FlipperListener**等都是为了处理不同物体碰撞的情况。  
**CurvedPipeCollisionFilter**, **PipeCollisionFilter**: 实现了**CollisionFilter**，球与管道、弯管道碰撞的过滤器。球实际会在管道中穿行，而不是发生碰撞，因此需要碰撞过滤。  
**BlackHole**，**ObstacleCircle**，**Ball**，**Flipper**，**ObstacleTriangle**，**Pipe**，**ObstacleRectangle**，**CurvedPipe**：分别代表黑洞，圆形障碍物，挡板，三角形障碍物，管道，矩形障碍物，弯管道的具体化物体。  

### UI层
**GridWorld**：继承了**GizmoWorld**，是**GizmoWorld**的网格化表示，对应实际设计模式下UI所呈现的世界。在此基础上加上了每个格子对应的**PhysicsBody**的信息以及边界信息。重写了添加物体的方法，添加的物体会同步到网格的信息，并且保证添加的物体位于网格上并且不会与其他已有物体重叠。  
**PlayerPanel**：前端控制器，用于接收前端操作指令。并且执行开始、暂停游戏，对物件的操作等。  
**GizmoOpHandler**：处理所有前端对**PhysicsBody**的操作，添加、删除、缩放、移动物体等。  
**CanvasRenderer**：渲染**PhysicsBody**的接口，不同物体可以有不同的渲染方式。  
**DefaultCanvasRenderer**：默认的Canvas渲染器，使用Canvas自带的绘制图形方法渲染**PhysicsBody**，只能渲染**AbstractShape**基础的形状，没有细节。  
**ImageRenderer**：通过图片（SVG例外）渲染**PhysicsBody**。  
**SVGRenderer**：通过SVG渲染**PhysicsBody**。  
**PersistentUtil**：用于保存、加载设计的物体。  

## 设计思想
弹球游戏相比之前的游戏比较复杂，考虑到在一般的游戏逻辑上需要加上对于碰撞的处理，我们额外引入了引擎层来处理物理碰撞相关问题。引擎层具体的实现参考了[dyn4j](https://github.com/dyn4j/dyn4j)。引擎层只负责一般的物理碰撞问题，而不考虑黑洞吸引、管道中匀速此类特殊的问题，为此我们引入了领域层，实现了具体的游戏功能。  
领域层具体化了游戏中用到的物件（墙壁，球，黑洞等），通过TickListener这种易于扩展的方式处理不同物体碰撞，并且实现了游戏逻辑。可以以代码的方式操作物体，以无界面方式运行。  
最后，为了可视化引入UI层。主要增加图形界面便于操作以及给GizmoWorld提供可视化。  
## 类图
![Gizmoball](https://fastly.jsdelivr.net/gh/D-Sketon/blog-img/Gizmoball_ClassModel.png)
