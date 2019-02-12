import * as React from 'react';
import {RowCenter} from '../../../components/layouts/row/Row';
import {LoadingSmall} from '../../../components/loading/Loading';
import {InfoText} from '../../../components/texts/Texts';
import {Styled} from '../../../types/Types';

interface Props extends Styled {
  isFetching: boolean;
  text: string;
  emptyContentTextStyle?: React.CSSProperties;
}

export const LoadingListItems = ({style}: Styled) =>
  <RowCenter style={style}><LoadingSmall/></RowCenter>;

export const LoadingListItemsWithEmptyText = ({isFetching, text, emptyContentTextStyle, style}: Props) =>
  isFetching
    ? <LoadingListItems style={style}/>
    : <InfoText className="first-uppercase" style={emptyContentTextStyle}>{text}</InfoText>;
