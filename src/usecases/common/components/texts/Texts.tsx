import * as React from 'react';
import './Texts.scss';

export const Normal = (props) => {
  const {className, children} = props;
  return (
    <div className={`Normal ${className || ''}`}>{children}</div>
  );
};

export const Bold = (props) => (<Normal className="Bold" {...props}/>);

export const Small = (props) => (<Normal className="Small" {...props}/>);

export const Xsmall = (props) => (<Normal className="Xsmall" {...props}/>);
