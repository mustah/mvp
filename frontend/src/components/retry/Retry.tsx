import * as React from 'react';
import {Maybe} from '../../helpers/Maybe';
import {ErrorResponse, OnClick} from '../../types/Types';
import {ButtonRetry} from '../buttons/ButtonRetry';
import {ColumnCenter} from '../layouts/column/Column';
import {RowCenter} from '../layouts/row/Row';
import 'Retry.scss';

interface Props {
  clearErrorAction: OnClick;
  error: Maybe<ErrorResponse>;
}

export const Retry = ({clearErrorAction, error}: Props) => {
  if (error.isJust()) {
    return (
      <ColumnCenter className="Retry">
        <RowCenter className="Retry-error-message">
          {error.get().message}
        </RowCenter>
        <ButtonRetry onClick={clearErrorAction} className="ButtonRetry"/>
      </ColumnCenter>
    );
  } else {
    return null;
  }
};
