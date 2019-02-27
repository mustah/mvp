import {ObjectsById} from '../state/domain-models/domainModels';
import {Identifiable, IdNamed, uuid} from '../types/Types';

export const toggle = <T>(item: T, list: T[]): T[] => {
  const asSet = new Set(list);
  if (!asSet.delete(item)) {
    asSet.add(item);
  }
  return Array.from(asSet);
};

export const removeAtIndex = <T>(items: T[], index: number): T[] => {
  if (index >= 0 || index < items.length) {
    items.splice(index, 1);
  }
  return items;
};

export const getId = (item: IdNamed | Identifiable): uuid => item.id;

export const groupById = <T extends Identifiable>(items: T[]): ObjectsById<T> =>
  items.reduce(
    (all: ObjectsById<T>, current: T) => {
      all[current.id] = current;
      return all;
    },
    {}
  );
