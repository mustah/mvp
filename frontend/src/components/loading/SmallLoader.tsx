import * as React from 'react';
import {Children, Styled} from '../../types/Types';
import {Row, RowCenter} from '../layouts/row/Row';
import {LoadingSmall} from './Loading';

interface Props extends Styled {
  children: Children;
  isFetching: boolean;
  loadingStyle?: React.CSSProperties;
}

export const SmallLoader = ({isFetching, children, loadingStyle, style}: Props) =>
  isFetching
    ? <RowCenter style={style}><LoadingSmall style={loadingStyle}/></RowCenter>
    : <Row>{children}</Row>;
