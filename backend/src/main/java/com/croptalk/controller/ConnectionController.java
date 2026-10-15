package com.croptalk.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croptalk.service.ConnectionService;
import com.croptalk.util.JsonUtil;

/** Port of routes/connections.js (mounted at /api/connections). */
@RestController
@RequestMapping("/api/connections")
public class ConnectionController {
    private final ConnectionService connectionService;

    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @PostMapping("/sendrequest/{clerkid}")
    public Map<String, Object> sendConnectionRequest(@PathVariable String clerkid,
                                                     @RequestBody(required = false) Map<String, Object> body) {
        return connectionService.sendConnectionRequest(clerkid, JsonUtil.orEmpty(body));
    }

    @PostMapping("/acceptrequest/{userclerkid}")
    public Map<String, Object> acceptConnectionRequest(@PathVariable String userclerkid,
                                                       @RequestBody(required = false) Map<String, Object> body) {
        return connectionService.acceptConnectionRequest(userclerkid, JsonUtil.orEmpty(body));
    }

    @PostMapping("/rejectrequest/{senderid}")
    public Map<String, Object> rejectConnectionRequest(@PathVariable String senderid,
                                                       @RequestBody(required = false) Map<String, Object> body) {
        return connectionService.rejectConnectionRequest(senderid, JsonUtil.orEmpty(body));
    }

    @PostMapping("/sendname/{receiverUsername}")
    public Map<String, Object> sendConnectionRequestByUsername(@PathVariable String receiverUsername,
                                                               @RequestBody(required = false) Map<String, Object> body) {
        return connectionService.sendConnectionRequestByUsername(receiverUsername, JsonUtil.orEmpty(body));
    }

    @GetMapping("/fetchrequests/{clerkid}")
    public List<Map<String, Object>> fetchConnectionRequests(@PathVariable String clerkid) {
        return connectionService.fetchConnectionRequests(clerkid);
    }

    @GetMapping("/fetchrequestsbyname/{username}")
    public Map<String, Object> fetchAllRequestsByUsername(@PathVariable String username) {
        return connectionService.fetchAllRequestsByUsername(username);
    }

    @GetMapping("/getAcceptedConnections/{userclerkid}")
    public Map<String, Object> fetchAcceptedConnections(@PathVariable String userclerkid) {
        return connectionService.fetchAcceptedConnections(userclerkid);
    }
}
