import * as React from 'react';
import {Children} from '../../types/Types';
import {Row} from '../layouts/row/Row';
import {LoadingSmall} from './Loading';

interface Props {
  children: Children;
  isFetching: boolean;
}

export const SmallLoader = ({isFetching, children}: Props) =>
  isFetching
    ? <Row className="SmallLoader"><LoadingSmall/></Row>
    : <Row>{children}</Row>;
