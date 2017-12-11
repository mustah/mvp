import * as React from 'react';
import {translate} from '../../services/translationService';
import './Content.scss';
import classNames = require('classnames');

interface Props {
  children: React.ReactElement<any>;
  hasContent: boolean;
  noContentTextKey: string;
  className?: string;
}

export const Content = (props: Props) => {
  const {className, hasContent, noContentTextKey, children} = props;

  if (!hasContent) {
    return (<h2 className={classNames('Content first-uppercase', className)}>{translate(noContentTextKey)}</h2>);
  } else {
    return children;
  }
};
