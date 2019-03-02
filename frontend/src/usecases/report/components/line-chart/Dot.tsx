import * as React from 'react';
import {uuid} from '../../../../types/Types';

export type KeyedDotProps = DotProps & {dataKey: uuid};

interface DotProps {
  cx: number;
  cy: number;
  index: number;
  stroke: any;

  [key: string]: any;
}

export const Dot = ({cx, cy, stroke}: DotProps) =>
  cy ? <circle cx={cx} cy={cy} r={1.3} fill={stroke} stroke={stroke}/> : null;
