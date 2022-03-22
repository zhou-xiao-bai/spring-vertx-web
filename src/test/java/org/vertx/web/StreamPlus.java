package org.vertx.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.LongStream;

/**
 * Unit test for simple App.
 */
public class StreamPlus
{
    public static void main(String[] args){
        List<Integer> array = new ArrayList<Integer>();

        array.add(123);
        array.add(456);

        StreamPlus.forEach(array, (node) -> {
            System.out.println(node.index() + ", " + node.obj());
            node.remove();
        });
        System.out.println(array.size());
    }

    /**
     * 
     * @param <T>
     * @param array 集合
     * @param action 
     */
    public static <T> void forEach(Collection<? extends T> array, Consumer<? super Node<T>> action) {
        Objects.requireNonNull(array);
        Objects.requireNonNull(action);
        int index = 0;
        
        //线程安全上锁
        synchronized(array){
            Iterator<T> it = (Iterator<T>) array.iterator();
            Node<T> node = new Node<>();
            while(it.hasNext()){
                T t = it.next();
                node.setIndex(index);
                node.setT(t);
                node.setIterator(it);
                action.accept(node);
                index++;
            }
        }

    }

    static class Node<T>{
        private T t;
        
        private int index;

        private Iterator<T> iterator;

        public T remove(){
            iterator.remove();
            return this.t;
        }

        public int index(){
            return index;
        }

        public T obj(){
            return this.t;
        }

        public void setT(T t) {
            this.t = t;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setIterator(Iterator<T> iterator) {
            this.iterator = iterator;
        }
    }
}