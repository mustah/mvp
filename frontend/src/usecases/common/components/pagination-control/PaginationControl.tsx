import * as React from 'react';
import {PaginationProps} from '../../../../state/ui/pagination/paginationModels';
import {Row} from '../layouts/row/Row';
import {Bold} from '../texts/Texts';
import './PaginationControl.scss';

export const PaginationControl = (props: PaginationProps) => {
  const {pagination: {page, limit}, changePage, numOfEntities} = props;
  const pages = Math.ceil(numOfEntities / limit);
  const changePageNext = () => changePage(page + 1);
  const changePagePrev = () => changePage(page - 1);
  const prevPage = page === 1 ? null : (
    <div onClick={changePagePrev} className="PaginationControl-button clickable">
      Previous page
    </div>
  );
  const nextPage = (page >= pages) ? null : (
    <div onClick={changePageNext} className="PaginationControl-button clickable">
      Next page
    </div>
  );
  return (
    <Row className="PaginationControl">
      {prevPage}
      <div>
        <Bold>{page} / {pages}</Bold>
      </div>
      {nextPage}
    </Row>
  );
};
