import {translate} from '../../services/translationService';
import {uuid} from '../../types/Types';
import {PieData, PieSlice} from './PieChartSelector';

const comparator = ({value: value1}: PieSlice, {value: value2}: PieSlice) =>
  (value1 < value2 ? 1 : value1 > value2 ? -1 : 0);

const bundleToOther = (data: PieSlice[]): PieSlice => {
  const bundleValueAndFilterParam = (prev, curr: PieSlice): PieSlice =>
    ({
      ...prev,
      value: prev.value + curr.value,
      filterParam: [...prev.filterParam, curr.filterParam],
    });
  const initBundle: PieSlice = {
    name: translate('other') || 'other',
    value: 0,
    filterParam: [],
  };
  return data.reduce(bundleValueAndFilterParam, initBundle);
};

export const splitDataIntoSlices = (
  segments: uuid[],
  data: PieData,
  maxSlices: number,
): PieSlice[] => {
  const pieSlicesSorted: PieSlice[] = segments
    .map((segment: uuid) => (data[segment]))
    .sort(comparator);

  if (segments.length > maxSlices) {
    const largestFields: PieSlice[] = pieSlicesSorted.slice(0, maxSlices - 1);
    const other: PieSlice[] = pieSlicesSorted.slice(maxSlices - 1);
    return [...largestFields, bundleToOther(other)];
  } else {
    return pieSlicesSorted;
  }
};
