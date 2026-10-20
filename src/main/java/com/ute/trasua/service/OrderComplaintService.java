package com.ute.trasua.service;

import com.ute.trasua.dto.complaint.ComplaintResolveRequest;
import com.ute.trasua.dto.complaint.ComplaintSubmitRequest;
import com.ute.trasua.dto.complaint.OrderComplaintDto;
import com.ute.trasua.entity.User;

import java.util.List;

public interface OrderComplaintService {

    OrderComplaintDto submitComplaint(String orderCode, ComplaintSubmitRequest request, User currentUser);

    OrderComplaintDto getComplaintByOrderCode(String orderCode);

    List<OrderComplaintDto> getAllComplaints(String status);

    List<OrderComplaintDto> getComplaintsByBranch(Long branchId, String status);

    OrderComplaintDto resolveComplaint(Long complaintId, ComplaintResolveRequest request, User adminUser);
}
