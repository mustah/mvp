import * as React from 'react';
import {uuid} from '../../../types/Types';

export interface ActiveDotPropsFromApi {
  dataKey: uuid;
  fill: any;
  cx: number;
  cy: number;
}

interface ActiveDotProps extends ActiveDotPropsFromApi {
  activeDataKey: uuid;
}

export const ActiveDot = ({dataKey, fill, cx, cy, activeDataKey}: ActiveDotProps) => {
  if (dataKey === activeDataKey) {
    return <circle r={3} stroke={fill} cx={cx} cy={cy} fill={fill}/>;
  } else {
    return null;
  }
};
