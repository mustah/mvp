import * as React from 'react';

export interface DotPropsFromApi {
  cx: number;
  cy: number;
  index: number;
  stroke: any;
}

export const Dot = ({cx, cy, stroke}: DotPropsFromApi) => {
  if (cy) {
    return (<circle cx={cx} cy={cy} r={1.3} fill={stroke} stroke={stroke}/>);
  } else {
    return null;
  }
};
