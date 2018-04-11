import * as React from 'react';
import {formatLabelTimeStamp} from '../../../helpers/dateHelpers';
import {ActivePayload} from '../containers/GraphContainer';

export const CustomizedTooltip = ({payload: {name}, dataKey, value, stroke}: ActivePayload) => (
  <div style={{backgroundColor: 'white', border: '1px solid black', padding: 10}}>
    <div>{formatLabelTimeStamp(name)}</div>
    <div><span style={{color: stroke}}>{`${dataKey}: `}</span> <span>{value}</span></div>
  </div>
);
