import * as React from 'react';
import {RowCenter} from '../../../components/layouts/row/Row';
import {LoadingSmall} from '../../../components/loading/Loading';
import {EmptyContentText} from './EmptyContentText';
import './EmptyContentText.scss';

interface Props {
  isFetching: boolean;
  text: string;
}

export const LoadingListItem = ({isFetching, text}: Props) => {
  return isFetching
    ? <RowCenter><LoadingSmall/></RowCenter>
    : <EmptyContentText text={text} key="empty-content-1"/>;
};
