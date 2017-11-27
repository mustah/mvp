import * as classNames from 'classnames';
import * as React from 'react';
import 'Wrapper.scss';
import {LayoutProps} from '../layout/Layout';

export const WrapperIndent = (props: LayoutProps) =>
  <div {...props} className={classNames(props.className, 'Wrapper-indent')}/>;
