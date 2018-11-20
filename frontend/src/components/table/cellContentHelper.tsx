import * as React from 'react';
import {timestamp} from '../../helpers/dateHelpers';
import {Children, UnixTimestamp} from '../../types/Types';
import {Error} from '../texts/Texts';

export const renderCreated = (created: UnixTimestamp, hasValues: boolean): Children => {
  const textual = hasValues
    ? timestamp(created * 1000)
    : <Error>{timestamp(created * 1000)}</Error>;
  return <td className="no-wrap" key="created">{textual}</td>;
};
