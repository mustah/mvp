import * as React from 'react';
import {formatLabelTimeStamp} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {ActiveDataPoint} from '../reportModels';

export const CustomizedTooltip = ({payload: {name}, dataKey, value, stroke}: ActiveDataPoint) => (
  <div style={{backgroundColor: 'white', border: '1px solid black', padding: 10}}>
    <div>{formatLabelTimeStamp(name)}</div>
    <div><span style={{color: stroke}}>{`${dataKey}: `}</span> <span>{roundMeasurement(value)}</span></div>
  </div>
);
