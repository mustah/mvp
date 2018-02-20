import * as React from 'react';
import './HasContent.scss';
import classNames = require('classnames');

interface Props {
  hasContent: boolean;
  noContentText: string;
  className?: string;
  children: React.ReactElement<any>;
}

export const HasContent = (props: Props) => {
  const {className, hasContent, noContentText, children} = props;

  if (!hasContent) {
    return (<h2 className={classNames('HasContent first-uppercase', className)}>{noContentText}</h2>);
  } else {
    return children;
  }
};
