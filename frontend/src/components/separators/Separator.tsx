import * as React from 'react';
import './Separator.scss';

interface Props {
  style?: React.CSSProperties;
}

export const Separator = (props: Props) => (
  <div className="Separator" {...props}/>
);
