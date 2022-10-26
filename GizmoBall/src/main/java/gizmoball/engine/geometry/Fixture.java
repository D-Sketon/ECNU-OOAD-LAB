package gizmoball.engine.geometry;

import gizmoball.engine.geometry.shape.AbstractShape;

@Deprecated
public class Fixture {

    /**
     * 一个Fixture包含一个简单形状
     */
    protected AbstractShape shape;

    /**
     * 是否是触发器（忽略碰撞）
     */
    protected boolean isTrigger;

    /**
     * 用户自定义数据
     */
    protected Object userData;

}
