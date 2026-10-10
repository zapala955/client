package ru.white.utils.movement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.white.auth.StrDec;

@Environment(EnvType.CLIENT)
public class Ad<T> implements List<T> {
   private final T[] f114;

   @Override
   public ListIterator<T> listIterator() {
      return new ad.bd<>(this.f114, 0);
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      for (Object v3 : c) {

         if (!this.contains(v3)) {

            return false;
         }
      }

      return true;
   }

   @Override
   public boolean addAll(Collection<? extends T> c) {
      UnsupportedOperationException result = new UnsupportedOperationException;

      result./* $VF: Unable to resugar constructor */<init>();
      throw result;
   }

   @Override
   public 

      return this.f114.length;
   }

   @Override
   public T set(int index, T element) {
      UnsupportedOperationException result = new UnsupportedOperationException;

      result./* $VF: Unable to resugar constructor */<init>();
      throw result;
   }

   @Override
   public Iterator<T> iterator() {
      ad.bd result = new ad.bd;

      result./* $VF: Unable to resugar constructor */<init>(this.f114, 0);
      return result;
   }

   @Override
   public ListIterator<T> listIterator(int index) {
      ad.bd result = new ad.bd;

      result./* $VF: Unable to resugar constructor */<init>(this.f114, index);
      return result;
   }

   @Override
   public int lastIndexOf(Object o) {
      for (int v2 = this.f114.length - 1; v2 >= 0; v2--) {
         if (o == this.f114[v2]) {
            int v3 = -2002668250;

            return v2;
         }
      }

      return -1;
   }

   @Override
   public boolean addAll(int index, Collection<? extends T> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean remove(Object o) {
      UnsupportedOperationException result = new UnsupportedOperationException();

      throw result;
   }

   @Override
   public int indexOf(Object o) {
      int v2 = 0;

      while (v2 < this.f114.length) {

         if (o == this.f114[v2]) {
            return v2;
         }

         v2++;
         
      }

      return -1;
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      UnsupportedOperationException result = new UnsupportedOperationException();
      int v2 = 1451632877;
      int temp1 = v2 * (v2 + 1);

      throw result;
   }

   public Ad(Collection<T> collection) {
      this((T[])collection.toArray());
   }

   @Override
   public boolean isEmpty() {
      boolean v2;
      if (this.f114.length == 0) {

         v2 = true;
      } else {
         v2 = false;
      }

      return v2;
   }

   @Override
   public boolean contains(Object o) {
      Object[] v2 = this.f114;
      int v3 = v2.length;

      int v4 = 0;

      while (v4 < v3) {
         Object v5 = v2[v4];
         if (o == v5) {
            return true;
         }

         v4++;
      }

      return false;
   }

   @Override
   public boolean add(T t) {
      UnsupportedOperationException result = new UnsupportedOperationException;

      result./* $VF: Unable to resugar constructor */<init>();
      throw result;
   }

   public Ad(T[] array) {
      this.f114 = (T[])array;
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      UnsupportedOperationException result = new UnsupportedOperationException();

      throw result;
   }

   public static <T> ad<T> copy(T[] array) {
      ad result = new ad;

      result./* $VF: Unable to resugar constructor */<init>((T[])Arrays.copyOf(array, array.length));

      return result;
   }

   @Override
   public void clear() {
      UnsupportedOperationException result = new UnsupportedOperationException;

      result./* $VF: Unable to resugar constructor */<init>();
      throw result;
   }

   @Override
   public List<T> subList(int fromIndex, int toIndex) {
      UnsupportedOperationException result = new UnsupportedOperationException;

      result./* $VF: Unable to resugar constructor */<init>();

      throw result;
   }

   @Override
   public Object[] toArray() {

      return Arrays.copyOf(this.f114, this.f114.length);
   }

   @Override
   public <T1> T1[] toArray(T1[] a) {

      return (T1[])this.toArray();
   }

   @Override
   public void add(int index, T element) {
      throw new UnsupportedOperationException();
   }

   @Override
   public T get(int index) {

      return this.f114[index];
   }

   @Override
   public T remove(int index) {
      throw new UnsupportedOperationException();
   }

   @Environment(EnvType.CLIENT)
   private static final class bd<T> implements ListIterator<T> {
      private final T[] f112;
      private int f113;

      @Override
      public void set(T t) {
         UnsupportedOperationException result = new UnsupportedOperationException;

         result./* $VF: Unable to resugar constructor */<init>();
         throw result;
      }

      @Override
      public T next() {
         if (!this.hasNext()) {
            NoSuchElementException result = new NoSuchElementException;

            result./* $VF: Unable to resugar constructor */<init>();
            throw result;
         } else {
            return this.f112[this.f113++];
         }
      }

      private bd(T[] array, int initialCursor) {
         this.f112 = (T[])array;
         this.f113 = initialCursor;
      }

      @Override
      public 

         return this.f113 - 1;
      }

      @Override
      public void remove() {
         UnsupportedOperationException result = new UnsupportedOperationException;

         result./* $VF: Unable to resugar constructor */<init>();
         throw result;
      }

      @Override
      public 

         return this.f113;
      }

      @Override
      public boolean hasPrevious() {
         return this.f113 > 0;
      }

      @Override
      public void add(T t) {
         UnsupportedOperationException result = new UnsupportedOperationException();

         throw result;
      }

      @Override
      public boolean hasNext() {
         boolean result;
         if (this.f113 < this.f112.length) {
            result = true;

            
         } else {
            result = false;
         }

         return result;
      }

      @Override
      public T previous() {
         boolean result = this.hasPrevious();

         if (!result) {
            throw new NoSuchElementException();
         } else {
            return this.f112[--this.f113];
         }
      }

      
   }
}
