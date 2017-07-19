import * as React from 'react';
import './Icons.scss';

export const Icon = (props) => (
  <div className={`Icon ${props.className || ''}`}/>
);
