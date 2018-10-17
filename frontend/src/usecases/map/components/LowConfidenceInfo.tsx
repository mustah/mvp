import './LowConfidenceInfo.scss';
import * as React from 'react';
import Control from 'react-leaflet-control';
import {Children} from '../../../types/Types';

interface Props {
  children?: Children;
}

export const LowConfidenceInfo = ({children}: Props) =>
  children
    ? (
      <Control position="topright" className="LowConfidence">
        <p>{children}</p>
      </Control>
    )
    : null;
