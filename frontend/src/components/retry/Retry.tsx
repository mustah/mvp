import * as React from 'react';
import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, OnClickEventHandler} from '../../types/Types';
import {ButtonRetry} from '../buttons/ButtonRetry';
import {ColumnCenter} from '../layouts/column/Column';
import {RowCenter} from '../layouts/row/Row';
import './Retry.scss';

export interface RetryProps {
  clearError: OnClickEventHandler;
  error: Maybe<ErrorResponse>;
}

export const Retry = ({clearError, error}: RetryProps) =>
  error.map(_ => (
    <ColumnCenter className="Retry" key="retry">
      <RowCenter className="Retry-error-message">
        {error.get().message}
      </RowCenter>
      <ButtonRetry onClick={clearError} className="ButtonRetry"/>
    </ColumnCenter>
  )).getOrElseNull();
