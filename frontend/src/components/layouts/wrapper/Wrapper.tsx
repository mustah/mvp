import * as classNames from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layout/Layout';
import './Wrapper.scss';

export const WrapperIndent = (props: LayoutProps) =>
  <div {...props} className={classNames(props.className, 'Wrapper-indent')}/>;
