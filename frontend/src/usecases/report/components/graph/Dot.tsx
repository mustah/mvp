import * as React from 'react';

export interface DotReChartProps {
  cx: number;
  cy: number;
  index: number;
  stroke: any;

  [key: string]: any;
}

export const Dot = ({cx, cy, stroke}: DotReChartProps) => {
  if (cy) {
    return (<circle cx={cx} cy={cy} r={1.3} fill={stroke} stroke={stroke}/>);
  } else {
    return null;
  }
};
