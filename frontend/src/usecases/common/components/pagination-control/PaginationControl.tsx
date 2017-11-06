import * as React from 'react';
import {PaginationProps} from '../../../../state/ui/pagination/paginationModels';
import {RowCenter} from '../layouts/row/Row';
import './PaginationControl.scss';
import {Bold} from '../texts/Texts';

export const PaginationControl = (props: PaginationProps) => {
  const {pagination: {page, limit}, changePage, numOfEntities} = props;
  const pages = Math.ceil(numOfEntities / limit);

  const changePageNext = () => changePage(page + 1);
  const changePagePrev = () => changePage(page - 1);

  const prevPage = page === 1 ? (
    <div className="PaginationControl-button unselectable">
      Previous page
    </div>
  ) : (
    <div onClick={changePagePrev} className="PaginationControl-button unselectable clickable">
      Previous page
    </div>
  );

  const nextPage = (page >= pages) ? (
    <div className="PaginationControl-button">
      Next page
    </div>
  ) : (
    <div onClick={changePageNext} className="PaginationControl-button unselectable clickable">
      Next page
    </div>
  );

  return (
    <RowCenter className="PaginationControl">
      {prevPage}
      <div>
        <Bold>{page} / {pages}</Bold>
      </div>
      {nextPage}
    </RowCenter>
  );
};
