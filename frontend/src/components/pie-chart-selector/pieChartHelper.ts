import {Pie, PieData2} from './PieChartSelector2';
import {uuid} from '../../types/Types';
import {translate} from '../../services/translationService';

const sortPieData = (data: Pie[]): Pie[] => {
  const sortFunction = ({value: value1}: Pie, {value: value2}: Pie) =>
    (value1 < value2 ? 1 : value1 > value2 ? -1 : 0);
  return data.sort(sortFunction);
};

const bundleToOther = (data: Pie[]): Pie => {
  const bundle = (prev, curr: Pie): Pie =>
    ({...prev, value: prev.value + curr.value, filterParam: [...prev.filterParam, curr.filterParam]});
  const initBundle: Pie = {
    name: translate('other'),
    value: 0,
    filterParam: [],
  };
  return data.reduce(bundle, initBundle);
};

export const splitDataIntoSlices = (fields: uuid[], data: PieData2, maxSlices: number): Pie[] => {

  const pieSlices: Pie[] = fields.map((field) => (data[field]));
  const pieSlicesSorted: Pie[] = sortPieData(pieSlices);

  if (fields.length >= maxSlices) {
    const largestFields: Pie[] = pieSlicesSorted.slice(0, maxSlices - 1);
    const other: Pie[] = pieSlicesSorted.slice(maxSlices - 1);
    return [...largestFields, bundleToOther(other)];
  } else {
    return pieSlices;
  }
};
