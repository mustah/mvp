import FlatButton from 'material-ui/FlatButton';
import * as React from 'react';
import {PaginationProps} from '../../../../state/ui/pagination/paginationModels';
import {RowCenter} from '../layouts/row/Row';
import './PaginationControl.scss';
import {translate} from '../../../../services/translationService';

export const PaginationControl = (props: PaginationProps) => {
    const {pagination: {page, limit}, changePage, numOfEntities} = props;
    const pages = Math.ceil(numOfEntities / limit);

    /**
     * I generally don't like "negative" variables, but I favor noPrev over hasPrev because the React component can use
     * the variable without !inverting it, and thus not be forced to re-render as often.
     */
    const noPrev = page === 1;
    const noNext = page >= pages;

    const changePagePrev = noPrev ? () => void(0) : () => changePage(page - 1);
    const changePageNext = noNext ? () => void(0) : () => changePage(page + 1);

    const numbers = (current: number, total: number): any[] => {
      const pages: Array<React.ReactElement<FlatButton | HTMLSpanElement>> = [];
      const visibilityProximity: number = 3;
      let lastPrintedAreDots = false;

      // current starts with 1, meaning page should too
      for (let page = 1; page <= total; page++) {
        if (page === current) {
          pages.push(<FlatButton className="PageNumber" disabled={true} key={page}>{page}</FlatButton>);
          // pages.push(<span>{page}</span>);
          lastPrintedAreDots = false;
        } else if (Math.abs(page - current) <= visibilityProximity
          || page <= visibilityProximity
          || page > (total - visibilityProximity)) {
          const cb = () => changePage(page);
          pages.push(<FlatButton className="PageNumber" disabled={false} key={page} onClick={cb}>{page}</FlatButton>);
          // pages.push(<span>{page}</span>);
          lastPrintedAreDots = false;
        } else if (!lastPrintedAreDots) {
          pages.push(<span key={page}>...</span>);
          lastPrintedAreDots = true;
        }
      }
      return pages;
    };

    const renderedNumbers = numbers(page, pages);

    return (
      <RowCenter className="PaginationControl">
        <FlatButton disabled={noPrev} onClick={changePagePrev} className="first-uppercase">
          {translate('previous')}
        </FlatButton>
        {renderedNumbers}
        <FlatButton disabled={noNext} onClick={changePageNext} className="first-uppercase">
          {translate('next')}
        </FlatButton>
      </RowCenter>
    );
  }
;
