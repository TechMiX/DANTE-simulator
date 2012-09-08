/*
 * Copyright 2007 Luis Rodero Merino.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Luis Rodero Merino if you need additional information or
 * have any questions. Contact information:
 * Email: lrodero AT gsyc.es
 * Webpage: http://gsyc.es/~lrodero
 * Phone: +34 91 488 8107; Fax: +34 91 +34 91 664 7494
 * Postal address: Desp. 121, Departamental II,
 *                 Universidad Rey Juan Carlos
 *                 C/Tulipán s/n, 28933, Móstoles, Spain 
 *       
 */

package es.ladyr.util.dataStructs;

import java.util.ArrayList; 
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.SortedSet;

// Ordered list. add(), remove() and contains() are O(log n). No duplicates allowed.
// Elements are searched using methods Collections.binarySearch(List,Object) or 
// Collections.binarySearch(List,Object,Comparator), depending on if a Comparator was provided
// to the constructor. If not, elements in list must implement the Comparable interface, and
// be mutually comparable.
// I can not use TreeSet, as I need random access.
public class SortedArrayList extends ArrayList implements SortedSet {
    
    /**
     * 
     */    
    private static final long serialVersionUID = 1L;
    
    protected Comparator comparator = null;
    
    public Comparator comparator(){
        return comparator;
    }
    
    public SortedArrayList(Comparator comparator, SortedArrayList array){
        
        this.comparator = comparator;
        
        Comparator arrayComp = array.comparator();

        if((arrayComp != null) && (arrayComp.equals(comparator)))
            super.addAll(array);
        else
            addAll(array); // Must reorder elements in array
    }
    
    public SortedArrayList(SortedArrayList array){
        // Already ordered
        super.addAll(array);
        comparator = array.comparator();        
    }
    
    public SortedArrayList(Collection collection){
         addAll(collection);
    }
    
    public SortedArrayList(Object[] array, boolean ordered){        
        if(!ordered) {
            Object[] copy = new Object[array.length];
            System.arraycopy(array, 0, copy, 0, array.length);
            Arrays.sort(copy);
            for(int index = 0; index < copy.length; index++)
                super.add(index, copy[index]);  
        } else {
            for(int index = 0; index < array.length; index++)
                super.add(index, array[index]);            
        }
        
    }
    
    public SortedArrayList(){}
    
    
    // Search using binary search. If comparator != null, search is performed using the comparator. If
    // no comparator is set, then elements in list must implement Comparable, and be mutually comparable.
    // Returns the position index of the element, if the list contains it. Otherwise, it returns -(position  + 1),
    // where position is the place where the element should be inserted to keep the proper ordering of
    // elements.
    private int binarySearch(Object element){
        return (comparator != null ? Collections.binarySearch(this,element,comparator) : Collections.binarySearch(this,element));
    }
    
    
    // Add element to the list. If element is already in the list, then it is not added.
    // Returns true if list was modified.
    public boolean add(Object element){        
        
        int position = binarySearch(element);
        
        if(position >= 0)
            return false;

        super.add(-(position+1), element);
        return true;
    }
    

    // Add elements to the list. Elements already in the list are not added.
    // Returns true if list was modified.
    public boolean addAll(Collection collection){
        
        if(collection == null)
            throw new NullPointerException();

        boolean modified = false;

        super.ensureCapacity(super.size() + collection.size());
        
        Object[] objectArray = collection.toArray();            
        for(int index = 0; index < objectArray.length; index++)
            if(add(objectArray[index]))
                modified = true;
        
        return modified;
        
    }
    

    // Remove element from list, in case it is present.
    // Returns true if list was modified.
    public boolean remove(Object element){
        
        int position = binarySearch(element);
        
        if(position < 0)
            return false;

        super.remove(position); 
        return true;
    }
    

    // Remove elements from list, in case they are present.
    // Returns true if list was modified.
    public boolean removeAll(Collection collection){
        
        if(collection == null)
            throw new NullPointerException();
        
        Object[] objectArray = collection.toArray();

        boolean modified = false;
        
        for(int index = 0; index < objectArray.length; index++)
            if(remove(objectArray[index]))
                modified = true;
        
        return modified;
    }
    

    // Remove all elements that are NOT in the Collection passed as parameter.
    // Returns true if list was modified.
    public boolean retainAll(Collection collection){
        
        if(collection == null)
            throw new NullPointerException();
        
        super.clear();
        return addAll(collection);        
        
    }    

    // Returns true if list contains the element.
    public boolean contains(Object element){
        return (binarySearch(element) >= 0);
    }    

    // Returns true if list contains all elements in collection.
    public boolean containsAll(Collection collection){
        
        if(collection == null)
            throw new NullPointerException();
        
        Object[] array = collection.toArray();

        for(int index = 0; index < array.length; index++)
            if(!contains(array[index]))
                return false;
        
        return true;
        
    }
    
    // Returns index in list of element.
    public int indexOf(Object element){        
        int position = binarySearch(element);        
        return (position >= 0 ? position : -1);        
    }
    
    
    // Returns index in list of element.
    public int lastIndexOf(Object element){
        return indexOf(element);
    }

    public Object first() {
        if(isEmpty())
            throw new NoSuchElementException();
        return get(0);
    }

    public Object last() {
        if(isEmpty())
            throw new NoSuchElementException();
        return get(size()-1);
    }

    public SortedSet subSet(Object arg0, Object arg1) {
        throw new UnsupportedOperationException();
    }

    public SortedSet headSet(Object arg0) {
        throw new UnsupportedOperationException();
    }

    public SortedSet tailSet(Object arg0) {
        throw new UnsupportedOperationException();
    }
    
    
    // Not to be allowed, it could break elements order in list.
    public Object set(int index, Object element){
        throw new UnsupportedOperationException("NOT ALLOWED TO INSERT IN SPECIFIC POSITION (use add(Object object) method)");
    }

    // Not to be allowed, it could break elements order in list.
    public void add(int index, Object element){
        throw new UnsupportedOperationException("NOT ALLOWED TO INSERT IN SPECIFIC POSITION (use add(Object object) method)");
    }

}
