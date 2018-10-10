import * as React from 'react';
import {timestamp} from '../../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../../helpers/formatters';
import {ActiveDataPoint} from '../../reportModels';

const style: React.CSSProperties = {
  backgroundColor: 'white',
  border: '1px solid black',
  padding: 10,
};

export const CustomizedTooltip = ({payload: {name}, dataKey, value, stroke}: ActiveDataPoint) => (
  <div style={style}>
    <div>{timestamp(name)}</div>
    <div><span style={{color: stroke}}>{`${dataKey}: `}</span> <span>{roundMeasurement(value)}</span></div>
  </div>
);
