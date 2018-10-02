import {default as classNames} from 'classnames';
import * as React from 'react';
import {LayoutProps} from '../layout/Layout';
import {Row} from '../row/Row';
import './Wrapper.scss';

export const WrapperIndent = (props: LayoutProps) =>
  <Row {...props} className={classNames(props.className, 'Wrapper-indent')}/>;
